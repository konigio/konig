package io.konig.content.gae;

/*
 * #%L
 * Konig Content System, Google App Engine implementation
 * %%
 * Copyright (C) 2015 - 2017 Gregory McFall
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.activation.MimetypesFileTypeMap;


import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;

import io.konig.content.Asset;
import io.konig.content.AssetBundle;
import io.konig.content.AssetBundleKey;
import io.konig.content.AssetMetadata;
import io.konig.content.CheckInBundleResponse;
import io.konig.content.ContentAccessException;
import io.konig.content.ContentSystem;
import io.konig.content.EtagFactory;
import io.konig.content.ZipArchive;
import io.konig.content.ZipItem;

public class GaeContentSystem implements ContentSystem {
	
	private static final String ASSET = "Asset";
	private static final String BODY = "body";
	private static final String CONTENT_TYPE = "contentType";
	private static final String METADATA = "Metadata";
	private static final String BUNDLE_NAME = "bundleName";
	private static final String BUNDLE_VERSION = "bundleVersion";
	private static final String ETAG = "etag";
	
	private static final int BATCH_SIZE = 100;
	
	private static final MimetypesFileTypeMap mimeTypes = new MimetypesFileTypeMap();
	static {
		mimeTypes.addMimeTypes(
			"text/html html\n" + 
			"image/png png\n" + 
			"image/bmp bmp\n" + 
			"image/gif gif\n" + 
			"image/jpeg jpeg jpg\n" + 
			"image/svg+xml svg\n" + 
			"image/tiff tiff\n" + 
			"text/css css"
		);
	}
	
	
	@Override
	public CheckInBundleResponse checkInBundle(AssetBundle bundle) throws ContentAccessException {
		

		List<AssetMetadata> metadataList = bundle.getMetadataList();
		Map<String,String> etagMap = etagMap(metadataList);
		
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
				
		Set<String> availableAssets = new HashSet<>();
		
		for (int i=0; i<metadataList.size(); ) {

			Query query = new Query(ASSET).setKeysOnly();
			List<Key> keyList = new ArrayList<>();
			for (int j=0; j<BATCH_SIZE && i<metadataList.size(); j++) {
				AssetMetadata meta = metadataList.get(i++);
				Key bodyKey = createAssetKey(meta.getEtag());
				keyList.add(bodyKey);
			}
			query.setFilter(new FilterPredicate(Entity.KEY_RESERVED_PROPERTY, FilterOperator.IN, keyList));
			
			Iterable<Entity> sequence = datastore.prepare(query).asIterable();
			for (Entity entity : sequence) {
				Key key = entity.getKey();
				String etag = key.getName();
				String path = etagMap.remove(etag);
				availableAssets.add(path);
			}
		}
		
		List<Entity> entityList = availableEntities(availableAssets, metadataList);

		if (!entityList.isEmpty()) {
			DatastoreService service = DatastoreServiceFactory.getDatastoreService();
			service.put(entityList);
		}

		CheckInBundleResponse response = new CheckInBundleResponse();
		List<String> missingAssets = new ArrayList<>(etagMap.values());
		response.setMissingAssets(missingAssets);

		return response;
	}
	private List<Entity> availableEntities(Set<String> available, List<AssetMetadata> metadataList) {
		List<Entity> list = new ArrayList<>();
		for (AssetMetadata metadata : metadataList) {
			if (available.contains(metadata.getPath())) {
				Key key = createMetadataKey(metadata);
				list.add(toMetadataEntity(key, metadata));
			}
		}
		return list;
	}
	private Map<String, String> etagMap(List<AssetMetadata> metadataList) {
		Map<String,String> map = new HashMap<>();
		for (AssetMetadata meta : metadataList) {
			String path = meta.getPath();
			String etag = meta.getEtag();
			map.put(etag, path);
		}
		return map;
	}
	@Override
	public int saveMetadata(AssetMetadata metadata) throws ContentAccessException {
		int status = 200;
		Key key = createMetadataKey(metadata);
		
		Entity entity = null;
		DatastoreService service = DatastoreServiceFactory.getDatastoreService();
		try {
			entity = service.get(key);
		} catch (EntityNotFoundException e) {
			entity = new Entity(key);
			status = 201;
		}
		
		patchMetadata(metadata, entity);
		service.put(entity);
		return status;

	}

	private void patchMetadata(AssetMetadata metadata, Entity entity) {
		
		entity.setProperty(BUNDLE_NAME, metadata.getBundleKey().getName());
		entity.setProperty(BUNDLE_VERSION, metadata.getBundleKey().getVersion());
		entity.setProperty(CONTENT_TYPE, metadata.getContentType());
		entity.setProperty(ETAG, metadata.getEtag());
	}

	@Override
	public AssetMetadata getMetadata(String path) throws ContentAccessException {
		
		Key key = createMetadataKey(path);
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		try {
			Entity e = datastore.get(key);
			return toMetadata(e);
		} catch (EntityNotFoundException e) {
			return null;
		}
	}
	private AssetMetadata toMetadata(Entity e) {
		Key key = e.getKey();
		String path = key.getName();
		AssetBundleKey bundleKey = toBundleKey(e);
		String etag = (String)e.getProperty(ETAG);
		String contentType = (String) e.getProperty(CONTENT_TYPE);
		
		AssetMetadata metadata = new AssetMetadata();
		metadata.setBundleKey(bundleKey);
		metadata.setEtag(etag);
		metadata.setPath(path);
		metadata.setContentType(contentType);
		
		
		return metadata;
	}

	private AssetBundleKey toBundleKey(Entity e) {
		String name = (String)e.getProperty(BUNDLE_NAME);
		String version = (String) e.getProperty(BUNDLE_VERSION);
		return new AssetBundleKey(name, version);
	}

	private Key createMetadataKey(AssetMetadata metadata) {
		StringBuilder builder = new StringBuilder();
		builder.append(metadata.getBundleKey().getName());
		builder.append('/');
		builder.append(metadata.getBundleKey().getVersion());
		builder.append('/');
		builder.append(metadata.getPath());
		
		return KeyFactory.createKey(METADATA, builder.toString());
	}


	
	private Key createMetadataKey(String path) {
		if (path.charAt(0)=='/') {
			path = path.substring(1);
		}
		
		return KeyFactory.createKey(METADATA, path);
	}

	@Override
	public int saveAsset(Asset asset) throws ContentAccessException {
		int status = saveMetadata(asset.getMetadata());
		saveAssetBody(asset);
		return status;

	}
	

	private int saveAssetBody(Asset asset) throws ContentAccessException {
		int status = 200;
		try {
			Entity entity = null;
			DatastoreService service = DatastoreServiceFactory.getDatastoreService();
			Key bodyKey = createAssetKey(asset.getMetadata().getEtag());
			try {
				entity = service.get(bodyKey);
				patchBody(entity, asset);
			} catch (EntityNotFoundException e) {
				entity = toBodyEntity(bodyKey, asset);
				status = 201;
			}
			service.put(entity);
			
			
			
		} catch (IOException e) {
			throw new ContentAccessException(e);
		}
		return status;
	}

	private void patchBody(Entity entity, Asset asset) throws IOException {
		
		entity.setProperty(CONTENT_TYPE, asset.getMetadata().getContentType());
		entity.setProperty(BODY, new Blob(asset.getBody()));
	}

	private Key createAssetKey(String etag) {
		return KeyFactory.createKey(ASSET, etag);
	}
	
	private Entity toMetadataEntity(Key key, AssetMetadata metadata)  {
		Entity e = new Entity(key);
		e.setProperty(BUNDLE_NAME, metadata.getBundleKey().getName());
		e.setProperty(BUNDLE_VERSION, metadata.getBundleKey().getVersion());
		e.setProperty(CONTENT_TYPE, metadata.getContentType());
		e.setProperty(ETAG, metadata.getEtag());
		return e;
	}

	private Entity toBodyEntity(Asset asset) throws IOException {
		Key key = createAssetKey(asset.getMetadata().getEtag());
		return toBodyEntity(key, asset);
	}

	private Entity toBodyEntity(Key key, Asset asset) throws IOException {
		Entity e = new Entity(key);
		Blob body = new Blob(asset.getBody());
		e.setProperty(BODY, body);
		e.setProperty(CONTENT_TYPE, asset.getMetadata().getContentType());
		return e;
	}

	@Override
	public Asset getAsset(String path) throws ContentAccessException {
		
		AssetMetadata metadata = getMetadata(path);
		if (metadata == null) {
			return null;
		}
		
		Key key = createAssetKey(metadata.getEtag());
		
		DatastoreService service = DatastoreServiceFactory.getDatastoreService();
		try {
			Entity entity = service.get(key);
			return toAsset(metadata, entity);
		} catch (EntityNotFoundException e) {
			return null;
		}
	}

	private Asset toAsset(AssetMetadata metadata, Entity entity) {
		
		Blob body = (Blob) entity.getProperty(BODY);
		return new Asset(metadata, body.getBytes());
	}
	
	
	
	@Override
	public int saveBundle(AssetBundleKey bundleKey, ZipArchive archive) throws ContentAccessException {

		ZipItem item = null;
		List<Entity> list = new ArrayList<>();
		while ( (item = archive.nextItem()) != null) {
			Asset asset = toAsset(mimeTypes, bundleKey, item);
			list.add(toMetadataEntity(asset));
			try {
				list.add(toBodyEntity(asset));
			} catch (IOException e) {
				throw new ContentAccessException(e);
			}
		}

		DatastoreService service = DatastoreServiceFactory.getDatastoreService();
		service.put(list);
		
		return 0;
	}
	private Entity toMetadataEntity(Asset asset) {
		Key key = createMetadataKey(asset.getMetadata());
		Entity e = new Entity(key);
		patchMetadata(asset.getMetadata(), e);
		return e;
	}
	private Asset toAsset(MimetypesFileTypeMap mimeMap, AssetBundleKey bundleKey, ZipItem item) throws ContentAccessException {
		
		byte[] body = toByteArray(item.getInputStream());
		
		String contentType = mimeMap.getContentType(item.getName());
		String etag = EtagFactory.createEtag(body);
		
		AssetMetadata metadata = new AssetMetadata();
		metadata.setBundleKey(bundleKey);
		metadata.setPath(item.getName());
		metadata.setContentType(contentType);
		metadata.setEtag(etag);
		return new Asset(metadata, body);
	}
	
	private byte[] toByteArray(InputStream is) throws ContentAccessException {
		try {
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		
			int nRead;
			byte[] data = new byte[16384];
		
			while ((nRead = is.read(data, 0, data.length)) != -1) {
			  buffer.write(data, 0, nRead);
			}
		
			buffer.flush();
		
			return buffer.toByteArray();
		} catch (IOException e) {
			throw new ContentAccessException(e);
		}
	}

}

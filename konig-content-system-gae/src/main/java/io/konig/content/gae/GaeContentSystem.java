package io.konig.content.gae;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import io.konig.content.CheckBundleResponse;
import io.konig.content.ContentAccessException;
import io.konig.content.ContentSystem;

public class GaeContentSystem implements ContentSystem {
	
	private static final String ASSET = "Asset";
	private static final String BODY = "body";
	private static final String CONTENT_TYPE = "contentType";
	private static final String METADATA = "Metadata";
	private static final String BUNDLE_NAME = "bundleName";
	private static final String BUNDLE_VERSION = "bundleVersion";
	private static final String ETAG = "etag";
	
	private static final int BATCH_SIZE = 100;

	@Override
	public CheckBundleResponse checkBundle(AssetBundle bundle) throws ContentAccessException {
		

		List<AssetMetadata> metadataList = bundle.getMetadataList();
		Map<String,String> etagMap = etagMap(metadataList);
		
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
				
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
				etagMap.remove(etag);
			}
		}

		CheckBundleResponse response = new CheckBundleResponse();
		List<String> missingAssets = new ArrayList<>(etagMap.values());
		response.setMissingAssets(missingAssets);
		
		return response;
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
		Key key = createMetadataKey(metadata.getPath());
		
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

	private Key createMetadataKey(String path) {
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
	
	private Entity toMetadataEntity(Key key, AssetMetadata metadata) throws IOException {
		Entity e = new Entity(key);
		e.setProperty(BUNDLE_NAME, metadata.getBundleKey().getName());
		e.setProperty(BUNDLE_VERSION, metadata.getBundleKey().getVersion());
		e.setProperty(CONTENT_TYPE, metadata.getContentType());
		e.setProperty(ETAG, metadata.getEtag());
		return e;
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

}

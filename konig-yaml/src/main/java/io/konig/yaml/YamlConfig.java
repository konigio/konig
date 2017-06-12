package io.konig.yaml;

import java.util.HashMap;
import java.util.Map;

public class YamlConfig {

	
	private Map<Class<?>, YamlSerializer> serializers = new HashMap<>();
	
	public void registerSerializer(Class<?> type, YamlSerializer serializer) {
		serializers.put(type, serializer);
	}
	
	public YamlSerializer getSerializer(Class<?> type) {
		YamlSerializer result = serializers.get(type.getName());
		
		if (result == null) {
			if (!type.isInterface()) {
				Class<?> superclass = type.getSuperclass();
				if (superclass != null) {
					result = getSerializer(superclass);
				}
			}
			Class<?>[] interfaceList = type.getInterfaces();
			for (Class<?> iface : interfaceList) {
				result = getSerializer(iface);
			}
		}
		
		return result;
	}

}

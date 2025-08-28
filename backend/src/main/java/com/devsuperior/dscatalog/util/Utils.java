package com.devsuperior.dscatalog.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.devsuperior.dscatalog.entities.Product;
import com.devsuperior.dscatalog.projections.ProductProjection;

public class Utils {

	//A intenção desse metodo é pegar a lista ordenada(ordered) e montar uma nova lista ordenada com base na outra lista desordenada(unordered)
	public static List<Product> replace(List<ProductProjection> ordered, List<Product> unordered) {
		
		//Copiar todos os produtos da lista unordered para um map
		Map<Long, Product> map = new HashMap<>();
		for (Product obj : unordered) {
			map.put(obj.getId(), obj);
		}
		
		//Percorrer a lista ordered e para cada obj dessa lista, pegar o id dele, acessar a entidade produto que esta no map correspondente a ele, e adiciona na lista result
		List<Product> result = new ArrayList<>();
		for (ProductProjection obj : ordered) {
			result.add(map.get(obj.getId()));
		}
		
		return result;
		
	}
	
	

}

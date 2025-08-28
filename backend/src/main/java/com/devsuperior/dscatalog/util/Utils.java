package com.devsuperior.dscatalog.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.devsuperior.dscatalog.projections.IdProjection;

public class Utils {

	//Uma lista de qualquer tipo ou subtipo de IdProjection ou seja um metodo genérico
	//A intenção desse metodo é pegar a lista ordenada(ordered) e montar uma nova lista ordenada com base na outra lista desordenada(unordered)
	public static <ID> List<? extends IdProjection<ID>> replace(List<? extends IdProjection<ID>> ordered, List<? extends IdProjection<ID>> unordered) {
		
		//Copiar todos os produtos da lista unordered para um map
		Map<ID, IdProjection<ID>> map = new HashMap<>();
		for (IdProjection<ID> obj : unordered) {
			map.put(obj.getId(), obj);
		}
		
		//Percorrer a lista ordered e para cada obj dessa lista, pegar o id dele, acessar a entidade produto que esta no map correspondente a ele, e adiciona na lista result
		List<IdProjection<ID>> result = new ArrayList<>();
		for (IdProjection<ID> obj : ordered) {
			result.add(map.get(obj.getId()));
		}
		
		return result;
		
	}
	
	

}

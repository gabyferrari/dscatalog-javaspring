package com.devsuperior.dscatalog.services;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.devsuperior.dscatalog.dto.CategoryDTO;
import com.devsuperior.dscatalog.dto.ProductDTO;
import com.devsuperior.dscatalog.entities.Category;
import com.devsuperior.dscatalog.entities.Product;
import com.devsuperior.dscatalog.projections.ProductProjection;
import com.devsuperior.dscatalog.repositories.CategoryRepository;
import com.devsuperior.dscatalog.repositories.ProductRepository;
import com.devsuperior.dscatalog.services.exceptions.DatabaseException;
import com.devsuperior.dscatalog.services.exceptions.ResourceNotFoundException;

import jakarta.persistence.EntityNotFoundException;

@Service
public class ProductService {
	
	@Autowired
	private ProductRepository repository;
	
	@Autowired
	private CategoryRepository categoryRepository;
	
	@Transactional(readOnly = true)
	public Page<ProductDTO> findAllPaged(Pageable pageable) {
		Page<Product> list = repository.findAll(pageable);
		return list.map(x -> new ProductDTO(x));
	}
	
	@Transactional(readOnly = true)
	public ProductDTO findById(Long id) {
		Optional<Product> obj = repository.findById(id);
		Product entity = obj.orElseThrow(() -> new ResourceNotFoundException("Entity not found"));
		return new ProductDTO(entity, entity.getCategories());
	}
	
	@Transactional
	public ProductDTO insert(ProductDTO dto) {
		Product entity = new Product();
		copyDtoToEntity(dto, entity);
		entity = repository.save(entity);
		return new ProductDTO(entity);
	}

	@Transactional
	public ProductDTO update(Long id, ProductDTO dto) {
		try {
			Product entity = repository.getReferenceById(id);
			copyDtoToEntity(dto, entity);
			entity = repository.save(entity);
			return new ProductDTO(entity);
		}
		catch (EntityNotFoundException e) {
			throw new ResourceNotFoundException("Id not found" + id);
		}
	}

	@Transactional(propagation = Propagation.SUPPORTS)
	public void delete(Long id) {
		if (!repository.existsById(id)) {
			throw new ResourceNotFoundException("Resource not found");
		}
		try {
	        repository.deleteById(id);    		
		}
	    catch (DataIntegrityViolationException e) {
	        throw new DatabaseException("Integrity violation");
	   	}
	}
	
	private void copyDtoToEntity(ProductDTO dto, Product entity) {
		entity.setName(dto.getName());
		entity.setDescription(dto.getDescription());
		entity.setPrice(dto.getPrice());
		entity.setImgUrl(dto.getImgUrl());
		entity.setDate(dto.getDate());
		
		entity.getCategories().clear();
		for (CategoryDTO catDto : dto.getCategories()) {
			Category category = categoryRepository.getReferenceById(catDto.getId());
			entity.getCategories().add(category);
		}
	}

	@Transactional(readOnly = true)
	public Page<ProductDTO> findAllPaged(String name, String categoryId, Pageable pageable) {	
		
		//preparar a lista de categoria
		List<Long> categoryIds = Arrays.asList();
		if (!"0".equals(categoryId)) {
			String[] vet = categoryId.split(",");
			List<String> list = Arrays.asList(vet);
			categoryIds = list.stream().map(x -> Long.parseLong(x)).toList();
		}
		
		//Fazer a consulta original da pagina
		Page<ProductProjection> page = repository.searchProducts(categoryIds, name, pageable);
		
		//Pegar os ids dos produtos
		List<Long> productIds = page.map(x -> x.getId()).toList();
		
		//Usar os ids dos produtos como argumento para chamar os produtos com as categorias buscando todos juntos
		List<Product> entities = repository.searchProductsWithCategories(productIds);
		
		//Converter as entidades para dto
		List<ProductDTO> dtos = entities.stream().map(p -> new ProductDTO(p, p.getCategories())).toList();
		
		//Gerar um novo obj de pagina aproveitando os dados de pagina da primeira pagina original
		Page<ProductDTO> pageDto = new PageImpl<>(dtos, page.getPageable(), page.getTotalElements());
		
		return pageDto;
	}

}

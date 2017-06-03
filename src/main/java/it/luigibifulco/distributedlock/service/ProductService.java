package it.luigibifulco.distributedlock.service;

import java.util.List;

import org.springframework.stereotype.Service;

import it.luigibifulco.distributedlock.entity.Product;

@Service
public interface ProductService {

	public Product insertProduct();
	
	public List<String> listAllProducts();

	long deleteAll();

}

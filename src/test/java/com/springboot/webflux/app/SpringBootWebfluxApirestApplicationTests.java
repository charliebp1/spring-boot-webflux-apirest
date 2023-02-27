package com.springboot.webflux.app;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.webflux.app.models.documents.Categoria;
import com.springboot.webflux.app.models.documents.Producto;
import com.springboot.webflux.app.models.services.ProductoService;

import reactor.core.publisher.Mono;

@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class SpringBootWebfluxApirestApplicationTests {
	
	@Autowired
	private WebTestClient client;

	@Autowired
	private ProductoService service;
	
	@Value("${config.base.endpoint}")
	private String url;
	
	@SuppressWarnings("deprecation")
	@Test
	void listarTest() {
		client.get()
			  .uri(url)
			  .accept(MediaType.APPLICATION_JSON_UTF8)
			  .exchange()
			  .expectStatus().isOk()
			  .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
			  .expectBodyList(Producto.class)
			  .consumeWith(response -> {
				  List<Producto> productos = response.getResponseBody();
				  productos.forEach(p -> {
					  System.out.println(p.getNombre());
				  });
				  assertThat(productos.size()>0).isTrue();
			  });
			  //.hasSize(10);
	}
	
	@SuppressWarnings("deprecation")
	@Test
	void verTest() {
		
		Producto producto = service.findByNombre("Apple iPod").block();
		
		client.get()
			  .uri(url+"/{id}", Collections.singletonMap("id", producto.getId()))
			  .accept(MediaType.APPLICATION_JSON_UTF8)
			  .exchange()
			  .expectStatus().isOk()
			  .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
			  .expectBody(Producto.class)
			  .consumeWith(response -> {
				  Producto p = response.getResponseBody();
				  assertThat(p.getId()).isNotEmpty();
				  assertThat(p.getId().length()>0).isTrue();
				  assertThat(p.getNombre()).isEqualTo("Apple iPod");
			  });
			  //			  .expectBody()
			  //			  .jsonPath("$.id").isNotEmpty()
			  //			  .jsonPath("$.nombre").isEqualTo("Apple iPod");
	}
	
	@Test
	public void crearTest() {
		Categoria categoria = service.findCategoriaByNombre("Muebles").block();
		
		Producto producto = new Producto("Mesa Comedor", 150.00, categoria);
		
		client.post().uri(url)
		.contentType(MediaType.APPLICATION_JSON_UTF8)
		.accept(MediaType.APPLICATION_JSON_UTF8)
		.body(Mono.just(producto), Producto.class)
		.exchange()
		.expectStatus().isCreated()
		  .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
		  .expectBody()
		  .jsonPath("$.producto.id").isNotEmpty()
		  .jsonPath("$.producto.nombre").isEqualTo("Mesa Comedor")
		  .jsonPath("$.producto.categoria.nombre").isEqualTo("Muebles");
	}
	
	@Test
	public void crear2Test() {
		Categoria categoria = service.findCategoriaByNombre("Muebles").block();
		
		Producto producto = new Producto("Mesa Comedor", 150.00, categoria);
		
		client.post().uri(url)
		.contentType(MediaType.APPLICATION_JSON_UTF8)
		.accept(MediaType.APPLICATION_JSON_UTF8)
		.body(Mono.just(producto), Producto.class)
		.exchange()
		.expectStatus().isCreated()
		  .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
		  .expectBody(new ParameterizedTypeReference<LinkedHashMap<String, Object>>() {})
		  .consumeWith(response -> {
			  Object o = response.getResponseBody().get("producto");
			  Producto p = new ObjectMapper().convertValue(o, Producto.class);
			  assertThat(p.getId()).isNotEmpty();
			  assertThat(p.getNombre()).isEqualTo("Mesa Comedor");
			  assertThat(p.getCategoria().getNombre()).isEqualTo("Muebles");
			  assertThat(p.getId().length()>0).isTrue();
			 
		  });
	}
	
	@Test
	public void editarTest() {
		
		Producto producto = service.findByNombre("Lenovo Notebook").block();
		Categoria categoria = service.findCategoriaByNombre("Electronica").block();
		
		Producto productoEditado = new Producto("Gigabyte Notebook", 800.00, categoria);
		
		client.put().uri(url+"/{id}", Collections.singletonMap("id", producto.getId()))
		.contentType(MediaType.APPLICATION_JSON_UTF8)
		.accept(MediaType.APPLICATION_JSON_UTF8)
		.body(Mono.just(productoEditado), Producto.class)
		.exchange()
		.expectStatus().isCreated()
		  .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
		  .expectBody()
		  .jsonPath("$.id").isNotEmpty()
		  .jsonPath("$.nombre").isEqualTo("Gigabyte Notebook")
		  .jsonPath("$.categoria.nombre").isEqualTo("Electronica");
	}
	
	@Test
	public void eliminarTest() {
		Producto producto = service.findByNombre("Mica comoda 5 cajones").block();
		client.delete()
			.uri(url+"/{id}", Collections.singletonMap("id", producto.getId()))
			.exchange()
			.expectStatus().isNoContent()
			.expectBody()
			.isEmpty();
		
		client.get()
		.uri(url+"/{id}", Collections.singletonMap("id", producto.getId()))
		.exchange()
		.expectStatus().isNotFound()
		.expectBody()
		.isEmpty();
	}
}

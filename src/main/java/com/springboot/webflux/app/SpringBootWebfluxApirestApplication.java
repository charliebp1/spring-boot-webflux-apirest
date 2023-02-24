package com.springboot.webflux.app;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

import com.springboot.webflux.app.models.documents.Categoria;
import com.springboot.webflux.app.models.documents.Producto;

import reactor.core.publisher.Flux;

@SpringBootApplication
public class SpringBootWebfluxApirestApplication implements CommandLineRunner {
	
	@Autowired
	private com.springboot.webflux.app.models.services.ProductoService service;
	
	@Autowired
	private ReactiveMongoTemplate mongoTemplate;
	
	private static final Logger log = LoggerFactory.getLogger(SpringBootWebfluxApirestApplication.class);
	
	public static void main(String[] args) {
		SpringApplication.run(SpringBootWebfluxApirestApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		mongoTemplate.dropCollection("productos").subscribe();
		mongoTemplate.dropCollection("categorias").subscribe();
		
		Categoria electronica = new Categoria("Electronica");
		Categoria deporte = new Categoria("Deporte");
		Categoria informatica = new Categoria("Informática");
		Categoria muebles = new Categoria("Muebles");
		
		Flux.just(electronica, deporte, informatica, muebles)
		    .flatMap(service::saveCategoria)
		    .doOnNext(c -> {
		    	log.info("Categoria creada: " + c.getNombre()+ " " + c.getId());
		    }).thenMany(
		    		Flux.just(new Producto("TV Panasonic Pantalla LCD", 456.80, electronica),
		  				  new Producto("Sony Camara HD Digital", 177.89, electronica),
		  				  new Producto("Apple iPod", 46.89, electronica), 
		  				  new Producto("Lenovo Notebook", 977.89, informatica),
		  				  new Producto("Hewlett Packard Impresora", 200.89, informatica),
		  				  new Producto("Bianchi bicicleta", 77.89, deporte),
		  				  new Producto("Patinete eléctrico", 499.99, deporte),
		  				  new Producto("HP Notebook Omen 17", 2500.89, informatica),
		  				  new Producto("Mica comoda 5 cajones", 150.89, muebles),
		  				  new Producto("TV Sony Bravia smart TV OLED 4k Ultra HD", 2225.89, electronica)
		  				  )
		  				  .flatMap(producto -> {
		  					  producto.setCreateAt(new Date());
		  					  return service.save(producto);
		  				  })	
		    ).subscribe(producto -> log.info("Insert: " + producto.getId() +" "+ producto.getNombre()));
		
	}

}


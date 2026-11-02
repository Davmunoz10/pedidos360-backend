package Pedidos360.config;

import Pedidos360.model.Producto;
import Pedidos360.repository.ProductoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner cargarDatos(ProductoRepository repo) {
        return args -> {
            if (repo.count() == 0) {
                repo.save(new Producto("Teclado mecánico", "Switch rojo, layout ES", new BigDecimal("39990"), 25));
                repo.save(new Producto("Mouse inalámbrico", "2.4GHz, 1600 DPI", new BigDecimal("14990"), 40));
                repo.save(new Producto("Monitor 24 pulgadas", "Full HD, 75Hz", new BigDecimal("109990"), 10));
            }
        };
    }
}
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package labs.pm.app;

import labs.pm.data.Product;
import java.math.BigDecimal;

/**
 *
 * @author Marti
 */
public class Shop {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        Product p1 = new Product();
        
        //p1.setId(22);
        //p1.setName("ola k ase, programanado llaba o que ase");
        p1.setPrice(BigDecimal.valueOf(1.99));
        
        System.out.println(String.format("id = %d, name = %s, price = %f, discount = %f", p1.getId(),p1.getName() ,p1.getPrice(),p1.getDiscount() ));
        
    }
    
}

/*
 * Copyright (C) 2021 Marti
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package labs.pm.app;

import labs.pm.data.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Locale;

/**
 * [@code Shop] Clase que Representa un Producto.
 *
 * @version 1.0
 * @author Marti
 */
public class Shop {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        ProductManager pm = new ProductManager(Locale.UK);

        //pm.createProduct(101, "Tea", BigDecimal.valueOf(1.99), Rating.NOT_RATE);
        
        //pm.parseProduct("D,101,Tea,1.99,0,0");
        //pm.parseProduct("F,104,Chocolate,0.99,0,2020-05-19");
        
        pm.printProductReport(101);
        
        pm.reviewProduct(101, Rating.FIVE_STAR, "CM 1");
        pm.reviewProduct(101, Rating.FOUR_STAR, "CM 2");
        pm.reviewProduct(101, Rating.ONE_STAR, "CM 3");
        pm.reviewProduct(101, Rating.THREE_STAR, "CM 4");
        pm.reviewProduct(101, Rating.TWO_STAR, "CM 5");
        pm.reviewProduct(101, Rating.NOT_RATE, "CM 6");
        pm.reviewProduct(101, Rating.THREE_STAR, "CM 7");
        
        pm.printProductReport(101);
        
        
        
        pm.createProduct(102,"Coffee",BigDecimal.valueOf(2.99),Rating.FOUR_STAR);
        
        pm.reviewProduct(102, Rating.FIVE_STAR, "CM 1");
        pm.reviewProduct(102, Rating.FOUR_STAR, "CM 2");
        pm.reviewProduct(102, Rating.ONE_STAR, "CM 3");
        pm.reviewProduct(102, Rating.THREE_STAR, "CM 4");
        pm.reviewProduct(102, Rating.TWO_STAR, "CM 5");
        pm.reviewProduct(102, Rating.NOT_RATE, "CM 6");
        pm.reviewProduct(102, Rating.THREE_STAR, "CM 7");
        
        
//        pm.parseReview("101,4,CM 2");
//        pm.parseReview("102,4,CM 2");
//        
        
        //pm.parseProduct("102,4,CM 2");
        
        pm.dumpData();
        pm.restoreData();

        
       //int x = 12;
       
        pm.printProductReport(102);
        pm.printProductReport(104);
      
        pm.printProducts(
          p-> p.getPrice().floatValue() < 2,      
          (p1,p2) -> {
            return p2.getRating().ordinal() - p1.getRating().ordinal();
        } ) ;
        
        pm.printProducts((p1,p2) -> {
            return p2.getPrice().compareTo(p1.getPrice());
        }) ;
         
        Comparator<Product> ratingSorter = (p1, p2) -> p2.getRating().ordinal()-p1.getRating().ordinal();
        Comparator<Product> priceSorter = (p1, p2) -> p2.getPrice().compareTo(p1.getPrice());

        pm.printProducts(p-> p.getPrice().floatValue() < 2,  ratingSorter.thenComparing(priceSorter));
        pm.printProducts(ratingSorter.thenComparing(priceSorter).reversed());

        pm.getDiscounts().forEach((rating,discount) -> System.out.println(rating+"\t"+discount));

    }

}

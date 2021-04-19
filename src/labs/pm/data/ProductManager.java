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
package labs.pm.data;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 *
 * @author Marti
 */
public class ProductManager {

//    private Product product;
//    private Review[] reviews = new Review[5];
    private Map<Product, List<Review>> products = new HashMap();
    private ResourceFormatter formatter;
    
    private Map<String, ResourceFormatter> formatters
            = Map.of("en-GB", new ResourceFormatter(Locale.UK),
                     "en-US", new ResourceFormatter(Locale.US),
                     "fr-FR", new ResourceFormatter(Locale.FRANCE),
                     "ru-RU", new ResourceFormatter(new Locale("ru","RU")),
                     "zh-CH", new ResourceFormatter(Locale.CHINA));
    
    
    public ProductManager(Locale locale) {
        this(locale.toLanguageTag());
    }
    
    public ProductManager(String lenguageTag) {
        changeLocale(lenguageTag);
    }
    
    public void changeLocale (String lenguageTag){
        formatter = formatters.getOrDefault(lenguageTag, formatters.get("en-GB"));
    } 

    public Product createProduct(int id, String name, BigDecimal price, Rating rating, LocalDate bestBefore) {
        Product product = new Food(id, name, price, rating, bestBefore);
        this.products.putIfAbsent(product, new ArrayList<>());
        return product;
    }

    public Product createProduct(int id, String name, BigDecimal price, Rating rating) {
        Product product = new Drink(id, name, price, rating);
        this.products.putIfAbsent(product, new ArrayList<>());
        return product;
    }

    public Product reviewProduct(int id, Rating rating, String comments) {
        return this.reviewProduct(this.findProduct(id), rating, comments);
    }

    public Product reviewProduct(Product product, Rating rating, String comments) {

//        if (reviews[reviews.length - 1] != null) {
//            Arrays.copyOf(reviews, reviews.length + 5);
//        }
        List<Review> reviews = products.get(product);
        products.remove(products, reviews);

        reviews.add(new Review(rating, comments));

        int sum = 0;

        for (Review review : reviews) {
            sum += review.getRating().ordinal();
        }

        product = product.applyRating(Rateable.convert(Math.round((float) sum / reviews.size())));

        products.put(product, reviews);

//        int sum = 0, i = 0;
//
//        boolean reviewed = false;
//
//        while (i < reviews.length && !reviewed) {
//            if (reviews[i] == null) {
//
//                reviews[i] = new Review(rating, comments);
//                reviewed = true;
//            }
//            sum += reviews[i].getRating().ordinal();
//            i++;
//        }
//
//        this.product = product.applyRating(Rateable.convert(Math.round((float) sum / i)));
//        return this.product;
        return product;
    }

    public Product findProduct(int id) {
        
        return products.keySet()
                .stream()
                .filter(p -> p.getId() == id)
                .findFirst()
                .orElseGet(()-> null);
        
//        Product result = null;
//        for (Product product : products.keySet()) {
//
//            if (product.getId() == id) {
//                result = product;
//                break;
//            }
//        }
//        return result;
    }

    public void printProductReport(int id) {
        this.printProductReport(this.findProduct(id));
    }

    public void printProductReport(Product product) {

        List<Review> reviews = products.get(product);

        StringBuilder txt = new StringBuilder();
        
        txt.append(formatter.formatProduct(product));
        
        
        
//
//        txt.append(MessageFormat.format(resources.getString("product"),
//                product.getName(),
//                moneyFormat.format(product.getPrice()),
//                product.getRating(),
//                product.getRating().getStart(),
//                dateFormat.format(product.getBestBefore())));

        txt.append("\n");
        Collections.sort(reviews);

//        if (reviews[0] != null) {
        if (reviews.isEmpty()) {
//            txt.append(resources.getString("no.review"));
            
            txt.append(formatter.getText("no.review"));
            txt.append("\n");
        }

        for (Review review : reviews) {
//            if (review == null) {
//                break;
//            } else if (review.getRating() == null) {
//                break;
//            }

            txt.append(formatter.formatReview(review));

            txt.append("\n");

        }
        System.out.println(txt);
    }
    
    public void printProducts(Comparator<Product> sorter){
        List<Product> productList = new ArrayList<>(products.keySet());
        productList.sort(sorter);
             
        StringBuilder txt = new StringBuilder();
        
        for(Product product: productList){
            txt.append(formatter.formatProduct(product));
            txt.append("\n");
        }
        
        System.out.println(txt);
        
    }

    private static class ResourceFormatter {

        private Locale locale;
        private ResourceBundle resources;
        private DateTimeFormatter dateFormat;
        private NumberFormat moneyFormat;

        private ResourceFormatter(Locale locale) {

            this.locale = locale;

            this.resources = ResourceBundle.getBundle("labs.pm.data.resources", locale);
            this.dateFormat = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).localizedBy(locale);
            this.moneyFormat = NumberFormat.getCurrencyInstance(locale);

        }
        
        private String formatProduct(Product product){
            return MessageFormat.format(resources.getString("product"),
                product.getName(),
                moneyFormat.format(product.getPrice()),
                product.getRating(),
                product.getRating().getStart(),
                dateFormat.format(product.getBestBefore()));

        }
        
        private String formatReview (Review review){
            return MessageFormat.format(resources.getString("review"), review.getRating().getStart(), review.getComments());
        }
        
        private String getText (String key){
            return resources.getString(key);
        }
    }

}


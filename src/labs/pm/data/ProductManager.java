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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author Marti
 */
public class ProductManager {

    private Map<Product, List<Review>> products = new HashMap();

    //private ResourceFormatter formatter;
    private final ResourceBundle config = ResourceBundle.getBundle("labs.pm.data.config");

    private final MessageFormat reviewFormat = new MessageFormat(config.getString("review.data.format"));
    private final MessageFormat productFormat = new MessageFormat(config.getString("product.data.format"));

    private final Path reportsFolder = Path.of(config.getString("reports.folder"));
    private final Path dataFolder = Path.of(config.getString("data.folder"));
    private final Path tempFolder = Path.of(config.getString("temp.folder"));

    private static final Map<String, ResourceFormatter> formatters
            = Map.of("en-GB", new ResourceFormatter(Locale.UK),
                    "en-US", new ResourceFormatter(Locale.US),
                    "fr-FR", new ResourceFormatter(Locale.FRANCE),
                    "ru-RU", new ResourceFormatter(new Locale("ru", "RU")),
                    "zh-CH", new ResourceFormatter(Locale.CHINA));

    private static final Logger logger = Logger.getLogger(ProductManager.class.getName());

    private static final ProductManager pm = new ProductManager();

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock writeLock = lock.writeLock();
    private final Lock readLock = lock.readLock();

    /*
    public ProductManager(/*Locale locale* /) {
        //this(locale.toLanguageTag());
    }*/

    private ProductManager(/*String lenguageTag*/) {
        //  this.changeLocale(lenguageTag);
        this.loadAllData();
    }

    public static ProductManager getInstance() {
        return pm;
    }

    public void changeLocale(/*String lenguageTag*/) {
        // formatter = formatters.getOrDefault(lenguageTag, formatters.get(lenguageTag));
    }
    
    public static Set<String> getSupportLocales(){
        
        return formatters.keySet(); 
    
    }

    public Product createProduct(int id, String name, BigDecimal price, Rating rating, LocalDate bestBefore) {
        Product product = null;
        try {
            writeLock.lock();
            product = new Food(id, name, price, rating, bestBefore);
            this.products.putIfAbsent(product, new ArrayList<>());
        } catch (Exception ex) {
            logger.log(Level.INFO, "Error adding product " + ex.toString());
        } finally {
            writeLock.unlock();
        }

        return product;
    }

    public Product createProduct(int id, String name, BigDecimal price, Rating rating) {
        Product product = null;
        try {
            product = new Drink(id, name, price, rating);
            this.products.putIfAbsent(product, new ArrayList<>());
        } catch (Exception ex) {
            logger.log(Level.INFO, "Error adding product " + ex.toString());
        } finally {
            writeLock.unlock();
        }
        return product;
    }

    public Product reviewProduct(int id, Rating rating, String comments) {
        try {
            writeLock.lock();
            return this.reviewProduct(this.findProduct(id), rating, comments);
        } catch (ProductManagerException ex) {
            logger.log(Level.INFO, ex.toString());
        }finally{
            writeLock.unlock();
        }
        return null;
    }

    public Product reviewProduct(Product product, Rating rating, String comments) {

        List<Review> reviews = products.get(product);
        products.remove(products, reviews);

        reviews.add(new Review(rating, comments));

        product = product.applyRating(
                Rateable.convert((int) Math.round(
                        reviews.stream()
                                .mapToInt(r -> r.getRating().ordinal())
                                .average().
                                orElse(0))));

        products.put(product, reviews);
        return product;
    }

    public Product findProduct(int id) throws ProductManagerException {
        
        try{
            readLock.lock();
             return products.keySet()
                .stream()
                .filter(p -> p.getId() == id)
                .findFirst()
                .orElseThrow(() -> new ProductManagerException("Product with id "+ id +" not found"));
 
        }finally{
            readLock.unlock();
        }
              //.get();
        //orElseGet(()-> null);

    }

    public void printProductReport(int id, String lenguageTag, String client) {
        try {
            readLock.lock();
            this.printProductReport(this.findProduct(id), lenguageTag,client);
        } catch (ProductManagerException ex) {
            logger.log(Level.INFO, ex.toString());
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Error printing product report " + ex.toString(), ex);
        }finally{
            readLock.unlock();
        }
    }

    private void printProductReport(Product product, String lenguageTag, String client) throws IOException {

        ResourceFormatter formatter = formatters.getOrDefault(lenguageTag, formatters.get("en-GB"));

        List<Review> reviews = products.get(product);

        //StringBuilder txt = new StringBuilder();
        Path productFile = reportsFolder.resolve(
                MessageFormat.format(config.getString("report.file"), product.getId(), client));

        try ( PrintWriter out = new PrintWriter(
                new OutputStreamWriter(Files.newOutputStream(productFile, StandardOpenOption.CREATE), "UTF-8"))) {

            out.append(formatter.formatProduct(product) + System.lineSeparator());

            out.append("\n");
            Collections.sort(reviews);

            out.append(reviews.isEmpty()
                    ? formatter.getText("no.review")
                    : reviews.stream().map(r -> formatter.formatReview(r) + "\n")
                            .collect(Collectors.joining()));
        }

        //System.out.println(txt);
    }

    public void printProducts(Comparator<Product> sorter, String lenguageTag) {

        ResourceFormatter formatter = formatters.getOrDefault(lenguageTag, formatters.get("en-GB"));

        StringBuilder txt = new StringBuilder();

        products.keySet()
                .stream()
                .sorted(sorter)
                // .filter(filter)
                .forEach(p -> txt.append(formatter.formatProduct(p) + "\n"));

        System.out.println(txt);

    }

    public void dumpData() {
        try {
            if (Files.notExists(tempFolder)) {
                Files.createDirectories(tempFolder);
            }

            String textData = MessageFormat.format(config.getString("temp.file"), "DumpBeta");

            Path tempFile = tempFolder.resolve(textData);
            try ( ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(tempFile, StandardOpenOption.CREATE))) {

                out.writeObject(products);
                // products = new HashMap<>();

            }
        } catch (IOException ex) {
            logger.log(Level.WARNING, "Error dumping data " + ex.getMessage(), ex);
        }
    }

    @SuppressWarnings("unchecked")
    public void restoreData() {
        try {
            Path tempFile = Files.list(tempFolder)
                    .filter(path -> path.getFileName().toString().endsWith("tmp"))
                    .findFirst().orElseThrow();

            try ( ObjectInputStream in
                    = new ObjectInputStream(Files.newInputStream(tempFile, StandardOpenOption.READ))) {

                products = (HashMap) in.readObject();

            }

        } catch (ClassNotFoundException | IOException ex) {

            logger.log(Level.WARNING, "Error dumping data " + ex.getMessage(), ex);
        }
    }

    public void printProducts(Predicate<Product> filter, Comparator<Product> sorter, String lenguageTag) {

        try{
            
            readLock.lock();
            
            ResourceFormatter formatter = formatters.getOrDefault(lenguageTag, formatters.get("en-GB"));

            StringBuilder txt = new StringBuilder();

            products.keySet()
                    .stream()
                    .sorted(sorter)
                    .filter(filter)
                    .forEach(p -> txt.append(formatter.formatProduct(p) + "\n"));

            System.out.println(txt);
            
        }finally{
            
            readLock.unlock();
            
        }

    }

    private void loadAllData() {
        try {

            this.products = Files.list(dataFolder)
                    .filter(file -> file.getFileName().toString().startsWith("product"))
                    .map(file -> loadProduct(file)).filter(product -> product != null)
                    .collect(Collectors.toMap(product -> product, product -> loadReviews(product)));

        } catch (IOException ex) {
            logger.log(Level.WARNING, "Error loading data " + ex.getMessage(), ex);
        }
    }

    private Product loadProduct(Path file) {
        Product product = null;

        try {
            product = parseProduct(Files.lines(dataFolder.resolve(file), Charset.forName("UTF-8")).findFirst().orElseThrow());
        } catch (IOException ex) {
            logger.log(Level.WARNING, "Error loading product " + ex.getMessage(), ex);
        }

        return product;
    }

    private List<Review> loadReviews(Product product) {

        List<Review> reviews = null;

        Path file = dataFolder.resolve(MessageFormat.format(config.getString("review.data.file"), product.getId()));

        if (!Files.exists(file)) {
            reviews = new ArrayList<>();
        } else {
            try {
                reviews = Files.lines(file, Charset.forName("UTF-8"))
                        .map(text -> this.parseReview(text))
                        .filter(review -> review != null)
                        .collect(Collectors.toList());
            } catch (IOException ex) {
                logger.log(Level.WARNING, "Error loading review " + ex.getMessage(), ex);
            }

        }

        return reviews;
    }

    public Review parseReview(String text) {
        Review review = null;
        try {
            Object[] values = reviewFormat.parse(text);
            review = new Review(Rateable.convert(Integer.parseInt((String) values[0])), (String) values[1]);
            //reviewProduct ( Integer.parseInt((String)values[0]), Rateable.convert(Integer.parseInt((String)values[1])), (String)values[2]);
        } catch (ParseException | NumberFormatException ex) {
            logger.log(Level.WARNING, "Error parsing review" + text);
        }
        return review;
    }

    public Product parseProduct(String text) {
        Product product = null;
        try {

            Object[] values = productFormat.parse(text);

            int id = Integer.parseInt((String) values[1]);
            String name = (String) values[2];
            BigDecimal price = BigDecimal.valueOf(Double.parseDouble((String) values[3]));
            Rating rating = Rateable.convert(Integer.parseInt((String) values[4]));

            switch ((String) values[0]) {
                case "D":
                    product = new Drink(id, name, price, rating);//this.createProduct(id, name, price, rating);
                    break;

                case "F":
                    LocalDate bestBefore = LocalDate.parse((String) values[5]);
                    product = new Food(id, name, price, rating, bestBefore); //this.createProduct(id, name, price, rating, bestBefore);
                    break;

            }

        } catch (ParseException | NumberFormatException | DateTimeParseException ex) {
            logger.log(Level.WARNING, "Error parsing review " + text);
        }

        return product;
    }

    public Map<String, String> getDiscounts(String lenguageTag) {

        try{
            readLock.lock();
            
            ResourceFormatter formatter = formatters.getOrDefault(lenguageTag, formatters.get("en-GB"));

            return products.keySet()
                .stream()
                .collect(
                        Collectors.groupingBy(
                                product -> product.getRating().getStars(),
                                Collectors.collectingAndThen(
                                        Collectors.summingDouble(
                                                product -> product.getDiscount().doubleValue()),
                                        discount -> formatter.moneyFormat.format(discount))));

        }finally{
            
            readLock.unlock();
            
        }
        
        
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

        private String formatProduct(Product product) {
            return MessageFormat.format(resources.getString("product"),
                    product.getName(),
                    moneyFormat.format(product.getPrice()),
                    product.getRating(),
                    product.getRating().getStars(),
                    dateFormat.format(product.getBestBefore()));

        }

        private String formatReview(Review review) {
            return MessageFormat.format(resources.getString("review"), review.getRating().getStars(), review.getComments());
        }

        private String getText(String key) {
            return resources.getString(key);
        }
    }

}

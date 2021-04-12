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
import static java.math.RoundingMode.HALF_UP;

/**
 *
 * @author Marti
 */
public class Product {
    
    private int id;
    private String name;
    private BigDecimal price;
    
    public static final BigDecimal DISCOUNT_RATE;
    
    static {
        DISCOUNT_RATE = BigDecimal.valueOf(0.1);
    }
    
            

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }
    
    
    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(final BigDecimal price) {
        this.price = price;
    }
    
    public BigDecimal getDiscount(){
        return price.multiply(DISCOUNT_RATE).setScale(2,HALF_UP);
    }
}

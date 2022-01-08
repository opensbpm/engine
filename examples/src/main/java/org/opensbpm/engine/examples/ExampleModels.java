/*******************************************************************************
 * Copyright (C) 2020 Stefan Sedelmaier
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.opensbpm.engine.examples;

import java.io.InputStream;

public class ExampleModels {

    public static InputStream getBookPage103() {
        return findResource("BookPage-103.xml");
    }

    public static InputStream getBookPage105() {
        return findResource("BookPage-105.xml");
    }

    public static InputStream getDienstreiseantrag() {
        return findResource("Dienstreiseantrag.xml");
    }

    public static InputStream getRechungslegung() {
        return findResource("Rechnungslegung.xml");
    }

    public static InputStream getRechungslegungWizard() {
        return findResource("Rechnungslegung_Wizard.xml");
    }

    public static InputStream findResource(String fileName) {
        return ExampleModels.class.getResourceAsStream("/org/opensbpm/engine/examples/" + fileName);
    }

    private ExampleModels() {
    }

}

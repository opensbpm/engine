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
package org.opensbpm.engine.core;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import org.junit.Test;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class ScriptEngineTest {

    @Test
    public void testGroovyScriptingEngine() {
        ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
        for (ScriptEngineFactory engineFactory : scriptEngineManager.getEngineFactories()) {
            System.out.println("" + engineFactory.getEngineName());
            for (String mimeType : engineFactory.getMimeTypes()) {
                System.out.println("" + mimeType);
            }
        }
        ScriptEngine scriptEngine = scriptEngineManager.getEngineByMimeType("application/x-groovy");
        assertThat(scriptEngine, is(notNullValue()));
        assertThat(scriptEngine.getFactory().getEngineName(), is("Groovy Scripting Engine"));

    }
}

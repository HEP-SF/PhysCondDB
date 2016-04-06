/**
 * 
 * This file is part of PhysCondDB.
 *
 *   PhysCondDB is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   PhysCondDB is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with PhysCondDB.  If not, see <http://www.gnu.org/licenses/>.
 **/
package conddb.data.utils.json;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.cfg.HandlerInstantiator;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
 
/**
 * @author formica
 *
 */
@Component
public class SpringBeanHandlerInstantiator extends HandlerInstantiator {
 
    private ApplicationContext applicationContext;
 
    @Autowired
    public SpringBeanHandlerInstantiator(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        System.out.println("Creating handler using context "+applicationContext);
    }
 

	@Override
	public JsonDeserializer<?> deserializerInstance(DeserializationConfig config,
			Annotated ann, Class<?> clazz) {
		try {
			System.out.println("Search for class "+clazz.getName());
            return (JsonDeserializer<?>) applicationContext.getBean(clazz);
        } catch (Exception e) {
            // Return null and let the default behavior happen
        }
        return null;
	}

	@Override
	public KeyDeserializer keyDeserializerInstance(DeserializationConfig config,
			Annotated ann, Class<?> clazz) {
		try {
			System.out.println("Search for class "+clazz.getName());
            return (KeyDeserializer) applicationContext.getBean(clazz);
        } catch (Exception e) {
            // Return null and let the default behavior happen
        }
        return null;
	}
	
	@Override
	public JsonSerializer<?> serializerInstance(SerializationConfig config,
			Annotated ann, Class<?> clazz) {
		try {
            return (JsonSerializer<?>) applicationContext.getBean(clazz);
        } catch (Exception e) {
            // Return null and let the default behavior happen
        }
        return null;
	}

	@Override
	public TypeIdResolver typeIdResolverInstance(MapperConfig<?> config,
			Annotated ann, Class<?> clazz) {
		try {
            return (TypeIdResolver) applicationContext.getBean(clazz);
        } catch (Exception e) {
            // Return null and let the default behavior happen
        }
        return null;
	}

	@Override
	public TypeResolverBuilder<?> typeResolverBuilderInstance(
			MapperConfig<?> config, Annotated ann, Class<?> clazz) {
		try {
            return (TypeResolverBuilder<?>) applicationContext.getBean(clazz);
        } catch (Exception e) {
            // Return null and let the default behavior happen
        }
        return null;
	}

}
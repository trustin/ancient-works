/*
 *   @(#) $Id: ConversionPathImpl.java 129 2005-11-14 09:35:57Z trustin $
 *
 *   Copyright 2004 Trustin Lee
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */
package net.gleamynode.conversion.support;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.gleamynode.conversion.ConversionException;
import net.gleamynode.conversion.ConversionPath;
import net.gleamynode.conversion.Converter;
import net.gleamynode.conversion.ConverterContext;

/**
 * An implementation of {@link ConversionPath}
 * 
 * @author Trustin Lee
 * @version $Rev: 129 $, $Date: 2005-11-14 18:35:57 +0900 (Mon, 14 Nov 2005) $
 */
public class ConversionPathImpl implements ConversionPath {

    private static final long serialVersionUID = 5330727099606439278L;

    private final List path = new ArrayList();

    public ConversionPathImpl() {
    }

    public void add(Converter converter) {
        path.add(converter);
    }
    
    public void remove() {
        path.remove(path.size() - 1);
    }
    
    public void setAll(ConversionPath path) {
        clear();
        this.path.addAll(path.toList());
    }
    
    public void clear() {
        this.path.clear();
    }

    public List toList() {
        return new ArrayList( path );
    }

    public Object convert(Object source, ConverterContext ctx) {
        if (path.size() == 0)
            throw new IllegalStateException();

        ((ConversionPathImpl) ctx.getPath()).setAll(this);
        
        Object result = source;
        try {
            for (Iterator i = path.iterator(); i.hasNext();) {
                result = ((Converter) i.next()).doConversion(result, ctx);
            }
        } catch (Exception e) {
            throw new ConversionException(e);
        }
        return result;
    }
    
    public void copy(Object source, Object target, ConverterContext ctx) {
        if (path.size() == 0)
            throw new IllegalStateException();

        ((ConversionPathImpl) ctx.getPath()).setAll(this);
        
        System.out.println(this);
        Object result = source;
        Iterator i = path.iterator();
        try {
            for (;;) {
                Converter c = (Converter) i.next();
                if (i.hasNext()) {
                    result = c.doConversion(result, ctx);
                } else {
                    c.doCopy(result, target, ctx);
                    break;
                }
            }
        } catch (Exception e) {
            throw new ConversionException(e);
        }
    }
    
    public String toString() {
        StringBuffer buf = new StringBuffer();
        for (Iterator i = path.iterator(); i.hasNext();) {
            Converter c = (Converter) i.next();
            buf.append(c.getSourceType());
            buf.append(" -> ");
            buf.append(c.getTargetType());
            if (i.hasNext()) {
                buf.append(", ");
            }
        }
        return buf.toString();
    }
    
    public Object clone() {
        ConversionPathImpl ret = new ConversionPathImpl();
        ret.path.addAll(this.path);
        return ret;
    }
}

/*
 * @(#) $Id: StringListConverter.java 2 2004-07-19 08:11:43Z trustin $
 */
package net.gleamynode.convert.impl;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Trustin Lee (http://gleamynode.net/)
 * @version $Rev: 2 $, $Date: 2004-07-19 17:11:43 +0900 (월, 19  7월 2004) $
 */
public class StringListConverter extends StringCollectionConverter {
	public StringListConverter() {
	}
	
	
	protected Collection newCollection() {
		return new ArrayList();
	}
}

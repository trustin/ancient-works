/*
 * @(#) $Id: StringSetConverter.java 2 2004-07-19 08:11:43Z trustin $
 */
package net.gleamynode.convert.impl;

import java.util.Collection;
import java.util.HashSet;

/**
 * @author Trustin Lee (http://gleamynode.net/)
 * @version $Rev: 2 $, $Date: 2004-07-19 17:11:43 +0900 (월, 19  7월 2004) $
 */
public class StringSetConverter extends StringCollectionConverter {
	public StringSetConverter() {
	}
	
	
	protected Collection newCollection() {
		return new HashSet();
	}
}

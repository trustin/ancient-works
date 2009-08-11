/*
 * @(#) $Id: OptionalConverter.java 2 2004-07-19 08:11:43Z trustin $
 */
package net.gleamynode.convert.impl.optional;

import net.gleamynode.convert.Converter;

/**
 * @author Trustin Lee (http://gleamynode.net/)
 * @version $Rev: 2 $, $Date: 2004-07-19 17:11:43 +0900 (월, 19  7월 2004) $
 */
public abstract class OptionalConverter extends Converter {

	protected OptionalConverter() {
	}

	public abstract Class getTargetClass();
}
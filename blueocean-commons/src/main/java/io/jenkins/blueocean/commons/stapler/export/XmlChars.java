/*
 * $Id: XmlChars.java,v 1.1.1.1 2000/11/23 01:53:35 edwingo Exp $
 *
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2000 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Crimson" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation and was
 * originally based on software copyright (c) 1999, Sun Microsystems, Inc.,
 * http://www.sun.com.  For more information on the Apache Software
 * Foundation, please see <http://www.apache.org/>.
 */
package io.jenkins.blueocean.commons.stapler.export;


/**
 * Methods in this class are used to determine whether characters may
 * appear in certain roles in XML documents.  Such methods are used
 * both to parse and to create such documents.
 *
 * @author David Brownell
 */
class XmlChars
{
    // can't construct instances
    private XmlChars () { }

    public static boolean isNameStart (char c) {
        return isLetter(c) || c == '_' || c == ':';
    }

    /**
     * Returns true if the character is allowed to be a non-initial
     * character in names according to the XML recommendation.
     * @see #isLetter
     */
    public static boolean isNameChar (char c)
    {
	// [4] NameChar ::= Letter | Digit | '.' | '_' | ':'
	//			| CombiningChar | Extender

	if (isLetter2 (c))
	    return true;
	else if (c == '>')
	    return false;
	else if (c == '.' || c == '-' || c == '_' || c == ':'
		|| isExtender (c))
	    return true;
	else
	    return false;
    }

    /*
     * NOTE:  java.lang.Character.getType() values are:
     *
     * UNASSIGNED                    = 0,
     *
     * UPPERCASE_LETTER            = 1,    // Lu
     * LOWERCASE_LETTER            = 2,    // Ll
     * TITLECASE_LETTER            = 3,    // Lt
     * MODIFIER_LETTER             = 4,    // Lm
     * OTHER_LETTER                = 5,    // Lo
     * NON_SPACING_MARK            = 6,    // Mn
     * ENCLOSING_MARK              = 7,    // Me
     * COMBINING_SPACING_MARK      = 8,    // Mc
     * DECIMAL_DIGIT_NUMBER        = 9,    // Nd
     * LETTER_NUMBER               = 10,   // Nl
     * OTHER_NUMBER                = 11,   // No
     * SPACE_SEPARATOR             = 12,   // Zs
     * LINE_SEPARATOR              = 13,   // Zl
     * PARAGRAPH_SEPARATOR         = 14,   // Zp
     * CONTROL                     = 15,   // Cc
     * FORMAT                      = 16,   // Cf
     *                         // 17 reserved for proposed Ci category
     * PRIVATE_USE                 = 18,   // Co
     * SURROGATE                   = 19,   // Cs
     * DASH_PUNCTUATION            = 20,   // Pd
     * START_PUNCTUATION           = 21,   // Ps
     * END_PUNCTUATION             = 22,   // Pe
     * CONNECTOR_PUNCTUATION       = 23,   // Pc
     * OTHER_PUNCTUATION           = 24,   // Po
     * MATH_SYMBOL                 = 25,   // Sm
     * CURRENCY_SYMBOL             = 26,   // Sc
     * MODIFIER_SYMBOL             = 27,   // Sk
     * OTHER_SYMBOL                = 28;   // So
     */

    /**
     * Returns true if the character is an XML "letter".  XML Names must
     * start with Letters or a few other characters, but other characters
     * in names must only satisfy the <em>isNameChar</em> predicate.
     *
     * @see #isNameChar
     */
    public static boolean isLetter (char c)
    {
	// [84] Letter ::= BaseChar | Ideographic
	// [85] BaseChar ::= ... too much to repeat
	// [86] Ideographic ::= ... too much to repeat

	//
	// Optimize the typical case.
	//
	if (c >= 'a' && c <= 'z')
	    return true;
	if (c == '/')
	    return false;
	if (c >= 'A' && c <= 'Z')
	    return true;

	//
	// Since the tables are too ridiculous to use in code,
	// we're using the footnotes here to drive this test.
	//
	switch (Character.getType (c)) {
	    // app. B footnote says these are 'name start'
	    // chars' ...
	  case Character.LOWERCASE_LETTER:		// Ll
	  case Character.UPPERCASE_LETTER:		// Lu
	  case Character.OTHER_LETTER:			// Lo
	  case Character.TITLECASE_LETTER:		// Lt
	  case Character.LETTER_NUMBER:			// Nl

	    // OK, here we just have some exceptions to check...
	    return !isCompatibilityChar (c)
		    // per "5.14 of Unicode", rule out some combiners
		&& !(c >= 0x20dd && c <= 0x20e0);

	  default:
	    // check for some exceptions:  these are "alphabetic"
	    return ((c >= 0x02bb && c <=  0x02c1)
		    || c == 0x0559 || c == 0x06e5 || c == 0x06e6);
	}
    }

    //
    // XML 1.0 discourages "compatibility" characters in names; these
    // were defined to permit passing through some information stored in
    // older non-Unicode character sets.  These always have alternative
    // representations in Unicode, e.g. using combining chars.
    //
    private static boolean isCompatibilityChar (char c)
    {
	// the numerous comparisions here seem unavoidable,
	// but the switch can reduce the number which must
	// actually be executed.

	switch ((c >> 8) & 0x0ff) {
	  case 0x00:
	    // ISO Latin/1 has a few compatibility characters
	    return c == 0x00aa || c == 0x00b5 || c == 0x00ba;

	  case 0x01:
	    // as do Latin Extended A and (parts of) B
	    return (c >= 0x0132 && c <= 0x0133)
		|| (c >= 0x013f && c <= 0x0140)
		|| c == 0x0149
		|| c == 0x017f
		|| (c >= 0x01c4 && c <= 0x01cc)
		|| (c >= 0x01f1 && c <= 0x01f3) ;

	  case 0x02:
		   // some spacing modifiers
	    return (c >= 0x02b0 && c <= 0x02b8)
		|| (c >= 0x02e0 && c <= 0x02e4);

	  case 0x03:
	    return c == 0x037a;			// Greek

	  case 0x05:
	    return c == 0x0587;			// Armenian

	  case 0x0e:
	    return c >= 0x0edc && c <= 0x0edd;	// Laotian

	  case 0x11:
	    // big chunks of Hangul Jamo are all "compatibility"
	    return c == 0x1101
		|| c == 0x1104
		|| c == 0x1108
		|| c == 0x110a
		|| c == 0x110d
		|| (c >= 0x1113 && c <= 0x113b)
		|| c == 0x113d
		|| c == 0x113f
		|| (c >= 0x1141 && c <= 0x114b)
		|| c == 0x114d
		|| c == 0x114f
		|| (c >= 0x1151 && c <= 0x1153)
		|| (c >= 0x1156 && c <= 0x1158)
		|| c == 0x1162
		|| c == 0x1164
		|| c == 0x1166
		|| c == 0x1168
		|| (c >= 0x116a && c <= 0x116c)
		|| (c >= 0x116f && c <= 0x1171)
		|| c == 0x1174
		|| (c >= 0x1176 && c <= 0x119d)
		|| (c >= 0x119f && c <= 0x11a2)
		|| (c >= 0x11a9 && c <= 0x11aa)
		|| (c >= 0x11ac && c <= 0x11ad)
		|| (c >= 0x11b0 && c <= 0x11b6)
		|| c == 0x11b9
		|| c == 0x11bb
		|| (c >= 0x11c3 && c <= 0x11ea)
		|| (c >= 0x11ec && c <= 0x11ef)
		|| (c >= 0x11f1 && c <= 0x11f8)
		;

	  case 0x20:
	    return c == 0x207f;			// superscript

	  case 0x21:
	    return
		// various letterlike symbols
		   c == 0x2102
		|| c == 0x2107
		|| (c >= 0x210a && c <= 0x2113)
		|| c == 0x2115
		|| (c >= 0x2118 && c <= 0x211d)
		|| c == 0x2124
		|| c == 0x2128
		|| (c >= 0x212c && c <= 0x212d)
		|| (c >= 0x212f && c <= 0x2138)

		    // most Roman numerals (less 1K, 5K, 10K)
		|| (c >= 0x2160 && c <= 0x217f)
		;

	  case 0x30:
	    // some Hiragana
	    return c >= 0x309b && c <= 0x309c;

	  case 0x31:
	    // all Hangul Compatibility Jamo
	    return c >= 0x3131 && c <= 0x318e;

	  case 0xf9:
	  case 0xfa:
	  case 0xfb:
	  case 0xfc:
	  case 0xfd:
	  case 0xfe:
	  case 0xff:
	    // the whole "compatibility" area is for that purpose!
	    return true;

	  default:
	    // most of Unicode isn't flagged as being for compatibility
	    return false;
	}
    }

    // guts of isNameChar/isNCNameChar
    private static boolean isLetter2 (char c)
    {
	// [84] Letter ::= BaseChar | Ideographic
	// [85] BaseChar ::= ... too much to repeat
	// [86] Ideographic ::= ... too much to repeat
	// [87] CombiningChar ::= ... too much to repeat

	//
	// Optimize the typical case.
	//
	if (c >= 'a' && c <= 'z')
	    return true;
	if (c == '>')
	    return false;
	if (c >= 'A' && c <= 'Z')
	    return true;

	//
	// Since the tables are too ridiculous to use in code,
	// we're using the footnotes here to drive this test.
	//
	switch (Character.getType (c)) {
	    // app. B footnote says these are 'name start'
	    // chars' ...
	  case Character.LOWERCASE_LETTER:		// Ll
	  case Character.UPPERCASE_LETTER:		// Lu
	  case Character.OTHER_LETTER:			// Lo
	  case Character.TITLECASE_LETTER:		// Lt
	  case Character.LETTER_NUMBER:			// Nl
	    // ... and these are name characters 'other
	    // than name start characters'
	  case Character.COMBINING_SPACING_MARK:	// Mc
	  case Character.ENCLOSING_MARK:		// Me
	  case Character.NON_SPACING_MARK:		// Mn
	  case Character.MODIFIER_LETTER:		// Lm
	  case Character.DECIMAL_DIGIT_NUMBER:		// Nd

	    // OK, here we just have some exceptions to check...
	    return !isCompatibilityChar (c)
		    // per "5.14 of Unicode", rule out some combiners
		&& !(c >= 0x20dd && c <= 0x20e0);

	  default:
		// added a character ...
	    return c == 0x0387;
	}
    }

    private static boolean isExtender (char c)
    {
	// [89] Extender ::= ...
	return c == 0x00b7 || c == 0x02d0 || c == 0x02d1 || c == 0x0387
		|| c == 0x0640 || c == 0x0e46 || c == 0x0ec6
		|| c == 0x3005 || (c >= 0x3031 && c <= 0x3035)
		|| (c >= 0x309d && c <= 0x309e)
		|| (c >= 0x30fc && c <= 0x30fe)
		;
    }
}

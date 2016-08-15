/**
 * CROSSFIRE JAPAN INCORPORATED
 * This source code is under GPL License.
 * info@crossfire.jp
 * Official web site
 * http://alinous.org
 * 
 *  Copyright (C) 2007 Tomohiro Iizuka
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.alinous.exec.validator;

public class ValidatorFactory
{
	public static IValidator getValidator(String validatorName)
	{
		String name = validatorName.toLowerCase();
		if(name.equals(IValidator.VALIDATOR_NOT_NULL)){
			return new NotNullValidator();
		}
		else if(name.equals(IValidator.VALIDATOR_MAX_LENGTH)){
			return new MaxlenValidator();
		}
		else if(name.equals(IValidator.VALIDATOR_EMAIL)){
			return new EmailValidator();
		}
		else if(name.equals(IValidator.VALIDATOR_INT)){
			return new IntValidator();
		}
		else if(name.equals(IValidator.VALIDATOR_UINT)){
			return new UnsignedIntValidator();
		}
		else if(name.equals(IValidator.VALIDATOR_DOUBLE)){
			return new DoubleValidator();
		}
		else if(name.equals(IValidator.VALIDATOR_UDOUBLE)){
			return new UnsignedDoubleValidator();
		}
		else if(name.equals(IValidator.VALIDATOR_NOT_HANKAKU)){
			return new HankakuValidator();
		}
		else if(name.equals(IValidator.VALIDATOR_ULONG)){
			return new UnsignedLongValidator();
		}
		else if(name.equals(IValidator.VALIDATOR_LONG)){
			return new LongValidator();
		}
		else if(name.equals(IValidator.VALIDATOR_NUMBER)){
			return new NumberValidator();
		}
		else if(name.equals(IValidator.VALIDATOR_INT_RANGE)){
			
		}
		else if(name.equals(IValidator.VALIDATOR_STR_RANGE)){
			
		}
		else if(name.equals(IValidator.VALIDATOR_REGEX)){
			return new RegexpValidator();
		}
		else if(name.equals(IValidator.VALIDATOR_CUSTOM)){
			return new CustomValidator();
		}
		else if(name.equals(IValidator.VALIDATOR_DUMMY)){
			return new DummyValidator();
		}
		
		return null;
	}
}

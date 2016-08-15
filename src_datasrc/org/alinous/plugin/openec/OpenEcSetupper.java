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
package org.alinous.plugin.openec;

import java.util.List;

import org.alinous.repository.AlinousSystemRepository;

public class OpenEcSetupper
{
	public static void setup(List<String> list)
	{
		list.add(AlinousSystemRepository.VALUES_TABLE);
		list.add(AlinousSystemRepository.FORM_VALUS_TABLE);
		list.add(AlinousSystemRepository.INNER_STATUS_TABLE);
		list.add(AlinousSystemRepository.BACKING_STATUS_TABLE);
		list.add(AlinousSystemRepository.SESSION_TABLE);
		
		
		list.add("BLOG_NEWS");
		list.add("BLOG_CATEGORY");
		list.add("BLOG_PING_TARGET");
		
		list.add("TASKS");
		list.add("SYSTEM_INFO");
		list.add("USERS");
		list.add("SERIALS");
		list.add("CATEGORY1");
		list.add("CATEGORY2");
		
		list.add("REGION_MASTER");
		list.add("PREF_MASTER");
		list.add("TEMPERATURE_CLASS");
		list.add("DELIVER_SHEET_CLASS");
		list.add("DELIVER_HANDLING");
		list.add("DELIVER_TIME_RANGE");
		list.add("LEAD_TIME");
		
		list.add("CUSTOMERS");
		list.add("CUSTOMER_PRE_REGIST");
		list.add("CUSTOMERS_DELIVER");
		
		list.add("SHIPPING_SETTING");
		list.add("DELIVER_TYPE");
		list.add("DELIVER_BY_PREF");
		list.add("WHOLE_SELLER");
		
		list.add("RECOMMEND");
		
		list.add("POINT_SETTING");
		
		list.add("SETTLEMENT");
		list.add("SETTLEMENT_ENABLED_DELIVER");
		list.add("SETTLEMENT_MARGIN");
		
		list.add("CUSTOMER_ORDER");
		list.add("ORDERED_LINEUP");
		
		list.add("CUSTOM_TYPE");
		list.add("CUSTOM_FIELD");
		list.add("CUSTOM_FIELD_VALUE");
		
		list.add("MAIL_TEMPLETE");
		list.add("COMMODITY_CONTENT");
		list.add("SCRIPT_CONTENT");
		
		list.add("COMPANY");
		list.add("LAW_DESCRIPTION");
		
		//list.add("COMMODITY");
		list.add("COMMODITY_LINEUP");
		list.add("COMMODITY_LINEUP_DELIVER");
		list.add("COMMODITY_CATEGORY");
		list.add("COMMODITY_SET_SELL");
		
		list.add("CATEGORY1_DESIGN");
		list.add("CATEGORY1_RECOMMEND");
		list.add("CATEGORY2_DESIGN");
		
		list.add("CART_ORDER");
		list.add("PREREGIST_CART_ORDER");
	
		list.add("REMISE_SETTING");
	}
}

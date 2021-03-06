/*
 * chombo: Hadoop Map Reduce utility
 * Author: Pranab Ghosh
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0 
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package org.chombo.mr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.chombo.util.Utility;

/**
 * Metadata based on schema JSON file. Enriched by stats data
 * @author pranab
 *
 */
public class FeatureSchema {
	private List<FeatureField> fields;
	private FeatureFieldCollection fieldCollection;

	public List<FeatureField> getFields() {
		return fields;
	}

	public void setFields(List<FeatureField> fields) {
		this.fields = fields;
	}
	
	/**
	 * process field collection element
	 */
	public void  initialize() {
		if (null == fields) {
			fields = new ArrayList<FeatureField>();
		}
		
		//create fields from collection field element
		if (null != fieldCollection) {
			for (int thisOrdinal : fieldCollection.getOrdinals() ) {
				FeatureField field = fieldCollection.createFeatureField(thisOrdinal);
				fields.add(field);
			}
		}
	}
	
	/**
	 * Enhance schema with stats data
	 * @param config
	 * @param statsFilePath
	 * @param delim
	 * @throws IOException
	 */
	public void processStats(Configuration config, String statsFilePath, String delim) throws IOException {
    	InputStream fs = Utility.getFileStream(config, statsFilePath);
    	BufferedReader reader = new BufferedReader(new InputStreamReader(fs));
    	String line = null; 
    	String[] items = null;
    	
    	while((line = reader.readLine()) != null) {
    		items = line.split(delim);
    		if (items[1].equals("0")) {
    			int ordinal = Integer.parseInt(items[0]);
    			double mean = Double.parseDouble(items[4]);
    			double variance = Double.parseDouble(items[5]);
    			double stdDev = Double.parseDouble(items[6]);
    			
    			FeatureField field = findFieldByOrdinal(ordinal);
    			field.setMean(mean);
    			field.setVariance(variance);
    			field.setStdDev(stdDev);
    		}
    	}		
    	reader.close();
	}
	
	/**
	 * get field from ordinal
	 * @param ordinal
	 * @return
	 */
	public FeatureField findFieldByOrdinal(int ordinal) {
		FeatureField selField = null;
		for (FeatureField field : fields) {
			if (field.getOrdinal() == ordinal) {
				selField = field;
				break;
			}
		}
		return selField;
	}

	/**
	 * find class attribute field
	 * @return
	 */
	public FeatureField findClassAttrField() {
		FeatureField classAttrField = null;
		for (FeatureField field : fields) {
			if (!field.isId() && !field.isFeature()) {
				classAttrField = field;
				break;
			}
		}	
		return classAttrField;
	}
	
	/**
	 * @param attrOrd
	 * @return
	 */
	public int getCardinalitySize(int attrOrd) {
		FeatureField field  = findFieldByOrdinal(attrOrd);
		return  field.getCardinality().size();
	}
	
	/**
	 * @param attrOrd
	 * @param attrVal
	 * @return
	 */
	public int getCardinalityIndex(int attrOrd, String attrVal) {
		FeatureField field  = findFieldByOrdinal(attrOrd);
		return  field.cardinalityIndex(attrVal);		
	}
	
}

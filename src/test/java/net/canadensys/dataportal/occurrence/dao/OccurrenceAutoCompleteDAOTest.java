package net.canadensys.dataportal.occurrence.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import net.canadensys.dataportal.occurrence.dao.OccurrenceAutoCompleteDAO;
import net.canadensys.dataportal.occurrence.model.UniqueValuesModel;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;

/**
 * Test Coverage : 
 * -Unaccented search
 * -Load all possible values for a field
 * @author canadensys
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/test-spring.xml" })
@TransactionConfiguration(transactionManager="hibernateTransactionManager")
public class OccurrenceAutoCompleteDAOTest extends AbstractTransactionalJUnit4SpringContextTests{
	
	@Autowired
	private OccurrenceAutoCompleteDAO autoCompleteOccurrenceDAO;
	
	private JdbcTemplate jdbcTemplate;
	
	@Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }
	
    @Before
    public void setup(){	
		//make sure the table is empty
		jdbcTemplate.update("DELETE FROM unique_values"); //unaccented_value
		//add controlled rows
		
    	jdbcTemplate.update("INSERT INTO unique_values (key,occurrence_count,value,unaccented_value) VALUES ('country',125,'Canada','canada')");
		jdbcTemplate.update("INSERT INTO unique_values (key,occurrence_count,value,unaccented_value) VALUES ('country',1,'canada','canada')");
		jdbcTemplate.update("INSERT INTO unique_values (key,occurrence_count,value,unaccented_value) VALUES ('country',8,'Côte d''Ivoire','cote d''ivoire')");
		jdbcTemplate.update("INSERT INTO unique_values (key,occurrence_count,value,unaccented_value) VALUES ('country',3,'Norway','norway')");
    }
    
	@Test
	public void testUnaccentedSearch(){
		List<String> expectedCountryList = new ArrayList<String>();
		expectedCountryList.add("Canada");
		expectedCountryList.add("canada");
		expectedCountryList.add("Côte d'Ivoire");
		String json = autoCompleteOccurrenceDAO.getSuggestionsFor("country", "c", true);
		
		try{
			JSONObject jsonObj = new JSONObject(json);
			JSONArray jsonRows = (JSONArray)jsonObj.get("rows");
			
			for(int i=0;i<jsonRows.length();i++){
				assertTrue(expectedCountryList.contains(
						((JSONObject)jsonRows.get(i)).get("value")));
			}
		}
		catch (Exception e) {
			fail();
		}
	}
	
	@Test
	public void testGetAllPossibleValues(){
		List<UniqueValuesModel> possibleValues = autoCompleteOccurrenceDAO.getAllPossibleValues("country");
		assertEquals(4,possibleValues.size());
	}
}

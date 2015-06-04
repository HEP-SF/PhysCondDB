package conddb.controllers.test;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.Arrays;

import org.hamcrest.Matchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import conddb.builders.GlobalTagBuilder;
import conddb.dao.controllers.GlobalTagController;
import conddb.data.GlobalTag;

/////@ActiveProfiles({ "dev", "h2" })
/////@RunWith(SpringJUnit4ClassRunner.class)
/////classes = {TestContext.class, WebApplicationContext.class},
/////locations = { "classpath:/spring/services-context.xml" }
/////@ContextConfiguration(classes = {WebApplicationContext.class})
/////@WebAppConfiguration
public class GlobalTagControllerTest {

    private MockMvc mockMvc;
    
    public static final MediaType APPLICATION_JSON_UTF8 = new MediaType(MediaType.APPLICATION_JSON.getType(), MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));
    
    @Autowired
    private GlobalTagController gtagMock;
    
//    @//Test
    public void findAll_GlobalTags() throws Exception {
        GlobalTag first = new GlobalTagBuilder()
        .withName("GTAG_01")
        .withDescription("test global tag 01")
        .withValidity(new BigDecimal(999))
        .withRelease("first release")
        .build();
        GlobalTag second = new GlobalTagBuilder()
        .withName("GTAG_02")
        .withDescription("test global tag 02")
        .withValidity(new BigDecimal(999))
        .withRelease("first release")
        .build();
 
        when(gtagMock.getGlobalTagByNameLike("GTAG%")).thenReturn(Arrays.asList(first, second));
 
        //TestUtil.APPLICATION_JSON_UTF8
        mockMvc.perform(get("/conddbweb/globaltag"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$", Matchers.hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("GTAG_01")))
                .andExpect(jsonPath("$[0].description", is("test global tag 01")))
                .andExpect(jsonPath("$[0].release", is("first release")))
                .andExpect(jsonPath("$[1].name", is("GTAG_02")))
                .andExpect(jsonPath("$[1].description", is("test global tag 02ß")))
                .andExpect(jsonPath("$[1].release", is("first release")));
 
        verify(gtagMock, times(1)).getGlobalTagByNameLike("GTAG%");
        verifyNoMoreInteractions(gtagMock);
    }
}

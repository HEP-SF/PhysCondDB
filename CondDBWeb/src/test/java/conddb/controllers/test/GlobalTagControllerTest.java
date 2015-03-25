package conddb.controllers.test;

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.Arrays;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.is;
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

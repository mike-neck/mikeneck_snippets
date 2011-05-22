/**
 * 
 */
package orz.mikeneck.social.test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConnection;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mike
 *
 */
public class HttpGetResourceTest {

	private static final String TEST_RESULT = "{\"test_result\" : \"ok\"}";

	private static final int PORT_NUMBER = 3000;

	private static final String LOCAL_HOST = "localhost";

	private static Server server;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		InetSocketAddress inet = new InetSocketAddress(LOCAL_HOST, PORT_NUMBER);
		server = new Server(inet);
		
		Handler handler = new RequestHandlerImpl();
		server.setHandler(handler);
		
		server.start();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		server.stop();
		server.destroy();
	}

	@Test
	public void testIsServerReady() throws Exception {
		URI uri = URIUtils.createURI("http", LOCAL_HOST, PORT_NUMBER, "/search", "user=mike_neck&start=10&end=20&keywords=google&keywords=グーグル", null);
		HttpGet hGet = new HttpGet(uri);
		HttpClient client = new DefaultHttpClient();
		HttpResponse res = client.execute(hGet);
		
		HttpEntity entity = res.getEntity();
		
		if(entity != null){
			StringBuilder builder = null;
			InputStream input = null;
			try {
				input = entity.getContent();
				InputStreamReader reader = new InputStreamReader(input, "UTF-8");
				builder = new StringBuilder();
				while(reader.ready())
					builder.append((char)reader.read());
			}finally{
				input.close();
			}
			assertThat("testIsServerReady", builder.toString(), is(TEST_RESULT));
		}else{
			fail();
		}
	}

	/**
	 * 
	 * @author mike
	 *
	 */
	private static class RequestHandlerImpl extends AbstractHandler {

		@Override
		public void handle(String target, Request baseRequest,
				HttpServletRequest request, HttpServletResponse response)
				throws IOException, ServletException {
			int line = 0;
			
			@SuppressWarnings("unchecked")
			Enumeration<String> keys = baseRequest.getParameterNames();
			
			while(keys.hasMoreElements()){
				String key = keys.nextElement();
				System.out.print((line++) + " : " + key + " -> ");
				List<String> values = Arrays.asList(baseRequest.getParameterValues(key));
				int count = 0;
				for(String value : values)
					System.out.print("value[" + (count++) + "]" + value + " ");
				System.out.println();
			}
			
			response.setCharacterEncoding("UTF-8");
			response.setContentType("application/json");
			response.setStatus(HttpServletResponse.SC_OK);
			Writer w = response.getWriter();
			w.write(TEST_RESULT);
			
			HttpConnection connection = HttpConnection.getCurrentConnection();
			Request req = connection.getRequest();
			req.setHandled(true);
		}
	}
}

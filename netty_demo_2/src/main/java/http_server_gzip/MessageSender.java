package http_server_gzip;


import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.zip.GZIPOutputStream;

/**
 * @Author: xiaohuan
 * @Date: 2020-01-26 18:23
 */
public class MessageSender {
	public static void main(String[] args) {
		httpMessageSend();
	}

	public static void httpMessageSend(){
		String message = "{'name':'小徐','age':'25','sex':'男','height':'171'}";
		try {
			sendHttp("http://127.0.0.1:9000/v3/v3", message);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * http发送util;
	 * @param
	 */
	//gzip 压缩发送

	public static void sendHttp(String url, String message) throws IOException {
		PostMethod postMethod = new PostMethod(url);
		postMethod.setContentChunked(true);
		postMethod.addRequestHeader("Accept", "text/plain");
		postMethod.setRequestHeader("Content-Encoding", "gzip");
		postMethod.setRequestHeader("Transfer-Encoding", "chunked");
		try {
			ByteArrayOutputStream originalContent = new ByteArrayOutputStream();
			GZIPOutputStream gzipOut = new GZIPOutputStream(originalContent);
			gzipOut.write(message.getBytes(Charset.forName("UTF-8")));
			gzipOut.finish();
			postMethod.setRequestEntity(new ByteArrayRequestEntity(originalContent.toByteArray(), "text/plain; charset=utf-8"));
		} catch (Exception e) {
			e.printStackTrace();

		}

		int retry = 0;
		do {
			try {
				HttpClient httpClient = new HttpClient();
				int status = httpClient.executeMethod(postMethod);
				if (HttpStatus.SC_OK == status) {
					System.out.println("send http success, url=" + url + ", content=" + message);
					String rsp = postMethod.getResponseBodyAsString();
					System.out.println("send http success, status is: " + status + ", response is: " + rsp);
					return;
				} else {
					String rsp = postMethod.getResponseBodyAsString();
					System.out.println("send http fail, status is: " + status + ", response is: " + rsp);
				}
			} catch (HttpException e) {
				System.out.println("http exception when send http.");
			} catch (IOException e) {
				System.out.println("io exception when send http.");
			} finally {
				postMethod.releaseConnection();
			}
			System.out.println("this is "+ retry + " time, try next");
		} while (retry++ < 3);
	}
}

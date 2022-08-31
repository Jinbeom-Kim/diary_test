package com.project.test.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.google.gson.Gson;
import com.project.test.vo.testVo;

@Controller
@RequestMapping("/coin")
public class testController {
	
	@RequestMapping(value = "coin", method = RequestMethod.POST)
	public String home() {
		return "coin/coin";
	}
	
	@RequestMapping(value = "getToken", method = RequestMethod.POST)
	@ResponseBody
	public Object getToken() {
		String accessKey = "8YPLLIKIXv0dUd7AeE1JSASqCZoirBjH6TlSrS7f";
		String secretKey = "8LPAXG4qXNlKIR866uspYsmqtZ9kjNeY78i9MdTK";
		
		Algorithm algorithm = Algorithm.HMAC256(secretKey);
		
		String jwtToken = JWT.create()
						.withClaim("access_key", accessKey)
						.withClaim("nonce", UUID.randomUUID().toString())
						.sign(algorithm);
		
		String authenticationToken = "Bearer " + jwtToken;

		List<testVo> list = new ArrayList<testVo>();
		testVo vo = new testVo();
		vo.setToken(authenticationToken);

		list.add(0, vo);
		
		return list;
	}
	
	@RequestMapping(value = "getMoney", method = RequestMethod.GET)
	@ResponseBody
	public Object getMoney() throws ParseException {
		String accessKey = "8YPLLIKIXv0dUd7AeE1JSASqCZoirBjH6TlSrS7f";
		String secretKey = "8LPAXG4qXNlKIR866uspYsmqtZ9kjNeY78i9MdTK";
        String serverUrl = "https://api.upbit.com";

        Algorithm algorithm = Algorithm.HMAC256(secretKey);
        String jwtToken = JWT.create()
                .withClaim("access_key", accessKey)
                .withClaim("nonce", UUID.randomUUID().toString())
                .sign(algorithm);

        String authenticationToken = "Bearer " + jwtToken;
        
        String str = "";

        try {
            HttpClient client = HttpClientBuilder.create().build();
            HttpGet request = new HttpGet(serverUrl + "/v1/accounts");
            request.setHeader("Content-Type", "application/json");
            request.addHeader("Authorization", authenticationToken);

            HttpResponse response = client.execute(request);
            HttpEntity entity = response.getEntity();
            str = EntityUtils.toString(entity, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return str;
	}
	
	@RequestMapping(value = "getList", method = RequestMethod.GET)
	@ResponseBody
	public Object getList() {
		String str = "";
		try {
			HttpClient client = HttpClientBuilder.create().build();
			HttpGet request = new HttpGet("https://api.upbit.com/v1/market/all");
			
			HttpResponse response = client.execute(request);
			HttpEntity entity = response.getEntity();
			str = EntityUtils.toString(entity, "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return str;
	}
	
	@RequestMapping(value = "getCoinRank", method = RequestMethod.GET)
	@ResponseBody
	public Object getCoinRank(@RequestParam(value="name") String name) {
		String str = "";
		try {
			HttpClient client = HttpClientBuilder.create().build();
			String url = "https://api.upbit.com/v1/candles/days?count=10&market="+name;
			HttpGet request = new HttpGet(url);
			
			HttpResponse response = client.execute(request);
			HttpEntity entity = response.getEntity();
			
			str = EntityUtils.toString(entity, "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return str;
	}
	
	@RequestMapping(value = "getCoinPrice", method = RequestMethod.GET)
	@ResponseBody
	public Object getCoinPrice(@RequestParam(value="name") String name) {
		String str = "";
		try {
			HttpClient client = HttpClientBuilder.create().build();
			String url = "https://api.upbit.com/v1/candles/minutes/1?count=8&market="+name;
			HttpGet request = new HttpGet(url);
			
			HttpResponse response = client.execute(request);
			HttpEntity entity = response.getEntity();
			
			str = EntityUtils.toString(entity, "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return str;
	}
	
	@RequestMapping(value = "coinBuyGood", method = RequestMethod.GET)
	@ResponseBody
	public Object coinBuyGood(@RequestParam(value="name") String name
							 ,@RequestParam(value="price") String price) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		String accessKey = "8YPLLIKIXv0dUd7AeE1JSASqCZoirBjH6TlSrS7f";
		String secretKey = "8LPAXG4qXNlKIR866uspYsmqtZ9kjNeY78i9MdTK";
        String serverUrl = "https://api.upbit.com";

        HashMap<String, String> params = new HashMap<>();
        params.put("market", name);
        params.put("side", "bid");
        //params.put("volume", "0.01");
        params.put("price", price);
        params.put("ord_type", "price");

        ArrayList<String> queryElements = new ArrayList<>();
        for(Map.Entry<String, String> entity : params.entrySet()) {
            queryElements.add(entity.getKey() + "=" + entity.getValue());
        }

        String queryString = String.join("&", queryElements.toArray(new String[0]));

        MessageDigest md = MessageDigest.getInstance("SHA-512");
        md.update(queryString.getBytes("UTF-8"));

        String queryHash = String.format("%0128x", new BigInteger(1, md.digest()));

        Algorithm algorithm = Algorithm.HMAC256(secretKey);
        String jwtToken = JWT.create()
                .withClaim("access_key", accessKey)
                .withClaim("nonce", UUID.randomUUID().toString())
                .withClaim("query_hash", queryHash)
                .withClaim("query_hash_alg", "SHA512")
                .sign(algorithm);

        String authenticationToken = "Bearer " + jwtToken;

        String str = "";
        try {
            HttpClient client = HttpClientBuilder.create().build();
            HttpPost request = new HttpPost(serverUrl + "/v1/orders");
            request.setHeader("Content-Type", "application/json");
            request.addHeader("Authorization", authenticationToken);
            request.setEntity(new StringEntity(new Gson().toJson(params)));

            HttpResponse response = client.execute(request);
            HttpEntity entity = response.getEntity();

            str = EntityUtils.toString(entity, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return str;
	}
	
	@RequestMapping(value = "orderCancle", method = RequestMethod.GET)
	@ResponseBody
	public Object orderCancle(@RequestParam(value="uuid") String uuid) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		String accessKey = "8YPLLIKIXv0dUd7AeE1JSASqCZoirBjH6TlSrS7f";
		String secretKey = "8LPAXG4qXNlKIR866uspYsmqtZ9kjNeY78i9MdTK";
        String serverUrl = "https://api.upbit.com";
        
        HashMap<String, String> params = new HashMap<>();
        params.put("uuid", uuid);

        ArrayList<String> queryElements = new ArrayList<>();
        for(Map.Entry<String, String> entity : params.entrySet()) {
            queryElements.add(entity.getKey() + "=" + entity.getValue());
        }

        String queryString = String.join("&", queryElements.toArray(new String[0]));

        MessageDigest md = MessageDigest.getInstance("SHA-512");
        md.update(queryString.getBytes("UTF-8"));

        String queryHash = String.format("%0128x", new BigInteger(1, md.digest()));

        Algorithm algorithm = Algorithm.HMAC256(secretKey);
        String jwtToken = JWT.create()
                .withClaim("access_key", accessKey)
                .withClaim("nonce", UUID.randomUUID().toString())
                .withClaim("query_hash", queryHash)
                .withClaim("query_hash_alg", "SHA512")
                .sign(algorithm);

        String authenticationToken = "Bearer " + jwtToken;

        String str = "";
        try {
            HttpClient client = HttpClientBuilder.create().build();
            HttpDelete request = new HttpDelete(serverUrl + "/v1/order?" + queryString);
            request.setHeader("Content-Type", "application/json");
            request.addHeader("Authorization", authenticationToken);

            HttpResponse response = client.execute(request);
            HttpEntity entity = response.getEntity();

            str = EntityUtils.toString(entity, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
		return str;
	}
	
	@RequestMapping(value = "sellCoin", method = RequestMethod.GET)
	@ResponseBody
	public Object sellCoin(@RequestParam(value="name") String name,
						   @RequestParam(value="price") double price,
						   @RequestParam(value="bal") double bal) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		String result = "";
		try {
			HttpClient client = HttpClientBuilder.create().build();
			HttpGet request = new HttpGet("https://api.upbit.com/v1/trades/ticks?count=1&market="+name);
			
			HttpResponse response = client.execute(request);
			HttpEntity entity = response.getEntity();
			String str = EntityUtils.toString(entity, "UTF-8");
            str = str.substring(1);
            str = str.substring(0, str.length() - 1);
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(str);
            JSONObject json = (JSONObject)obj;
            double nowPrice = (double)json.get("trade_price");
            double persent = ((nowPrice - price)/price)*100;
            
            if(persent > 0.5) {
            	result = coinSell(name, price, bal, persent);
            }else if(Math.abs(persent) > 1){
            	result = coinSell(name, price, bal, persent);
            }else {
            	result = "[{\"SELL\":\"NO\",\"per\":"+persent+"}]";
            }
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public String coinSell(String name, double price, double bal, double persent) throws NoSuchAlgorithmException, UnsupportedEncodingException, ParseException {
		String result = "";
		String accessKey = "8YPLLIKIXv0dUd7AeE1JSASqCZoirBjH6TlSrS7f";
		String secretKey = "8LPAXG4qXNlKIR866uspYsmqtZ9kjNeY78i9MdTK";
        String serverUrl = "https://api.upbit.com";

        HashMap<String, String> params = new HashMap<>();
        params.put("market", name);
        params.put("side", "ask");
        params.put("volume", Double.toString(bal));
        //params.put("price", Float.toString(price));
        params.put("ord_type", "market");

        ArrayList<String> queryElements = new ArrayList<>();
        for(Map.Entry<String, String> entity : params.entrySet()) {
            queryElements.add(entity.getKey() + "=" + entity.getValue());
        }

        String queryString = String.join("&", queryElements.toArray(new String[0]));

        MessageDigest md = MessageDigest.getInstance("SHA-512");
        md.update(queryString.getBytes("UTF-8"));

        String queryHash = String.format("%0128x", new BigInteger(1, md.digest()));

        Algorithm algorithm = Algorithm.HMAC256(secretKey);
        String jwtToken = JWT.create()
                .withClaim("access_key", accessKey)
                .withClaim("nonce", UUID.randomUUID().toString())
                .withClaim("query_hash", queryHash)
                .withClaim("query_hash_alg", "SHA512")
                .sign(algorithm);

        String authenticationToken = "Bearer " + jwtToken;

        try {
            HttpClient client = HttpClientBuilder.create().build();
            HttpPost request = new HttpPost(serverUrl + "/v1/orders");
            request.setHeader("Content-Type", "application/json");
            request.addHeader("Authorization", authenticationToken);
            request.setEntity(new StringEntity(new Gson().toJson(params)));

            HttpResponse response = client.execute(request);
            HttpEntity entity = response.getEntity();
            String str = EntityUtils.toString(entity, "UTF-8");
            //str = str.substring(1);
            //str = str.substring(0, str.length() - 1);
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(str);
            JSONObject json = (JSONObject)obj;
            String uuid = (String)json.get("uuid");
            
            result = "[{\"SELL\":\"YES\",\"uuid\":\""+uuid+"\",\"per\":"+persent+"}]";
        } catch (IOException e) {
            e.printStackTrace();
        }
		return result;
	}
	
	@RequestMapping(value = "orderJosa", method = RequestMethod.GET)
	@ResponseBody
	public Object orderJosa(@RequestParam(value="uuid") String uuid) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		String accessKey = "8YPLLIKIXv0dUd7AeE1JSASqCZoirBjH6TlSrS7f";
		String secretKey = "8LPAXG4qXNlKIR866uspYsmqtZ9kjNeY78i9MdTK";
        String serverUrl = "https://api.upbit.com";
        
        HashMap<String, String> params = new HashMap<>();
        params.put("uuid", uuid);

        ArrayList<String> queryElements = new ArrayList<>();
        for(Map.Entry<String, String> entity : params.entrySet()) {
            queryElements.add(entity.getKey() + "=" + entity.getValue());
        }

        String queryString = String.join("&", queryElements.toArray(new String[0]));

        MessageDigest md = MessageDigest.getInstance("SHA-512");
        md.update(queryString.getBytes("UTF-8"));

        String queryHash = String.format("%0128x", new BigInteger(1, md.digest()));

        Algorithm algorithm = Algorithm.HMAC256(secretKey);
        String jwtToken = JWT.create()
                .withClaim("access_key", accessKey)
                .withClaim("nonce", UUID.randomUUID().toString())
                .withClaim("query_hash", queryHash)
                .withClaim("query_hash_alg", "SHA512")
                .sign(algorithm);

        String authenticationToken = "Bearer " + jwtToken;
        
        String str = "";

        try {
            HttpClient client = HttpClientBuilder.create().build();
            HttpGet request = new HttpGet(serverUrl + "/v1/order?" + queryString);
            request.setHeader("Content-Type", "application/json");
            request.addHeader("Authorization", authenticationToken);

            HttpResponse response = client.execute(request);
            HttpEntity entity = response.getEntity();

            str = EntityUtils.toString(entity, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
		return str;
	}
	
	@RequestMapping(value = "sellCoinTest", method = RequestMethod.GET)
	@ResponseBody
	public Object sellCoinTest(@RequestParam(value="name") String name,
						   @RequestParam(value="price") double price,
						   @RequestParam(value="bal") double bal) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		String result = "";
		try {
        	String accessKey = "8YPLLIKIXv0dUd7AeE1JSASqCZoirBjH6TlSrS7f";
    		String secretKey = "8LPAXG4qXNlKIR866uspYsmqtZ9kjNeY78i9MdTK";
            String serverUrl = "https://api.upbit.com";
        	
        	HashMap<String, String> params = new HashMap<>();
            params.put("market", name);
            params.put("side", "ask");
            params.put("volume", Double.toString(bal));
            //params.put("price", Float.toString(price));
            params.put("ord_type", "market");

            ArrayList<String> queryElements = new ArrayList<>();
            for(Map.Entry<String, String> entity2 : params.entrySet()) {
                queryElements.add(entity2.getKey() + "=" + entity2.getValue());
            }

            String queryString = String.join("&", queryElements.toArray(new String[0]));

            MessageDigest md = MessageDigest.getInstance("SHA-512");
            md.update(queryString.getBytes("UTF-8"));

            String queryHash = String.format("%0128x", new BigInteger(1, md.digest()));

            Algorithm algorithm = Algorithm.HMAC256(secretKey);
            String jwtToken = JWT.create()
                    .withClaim("access_key", accessKey)
                    .withClaim("nonce", UUID.randomUUID().toString())
                    .withClaim("query_hash", queryHash)
                    .withClaim("query_hash_alg", "SHA512")
                    .sign(algorithm);

            String authenticationToken = "Bearer " + jwtToken;
            
            HttpClient client2 = HttpClientBuilder.create().build();
            HttpPost request2 = new HttpPost(serverUrl + "/v1/orders");
            request2.setHeader("Content-Type", "application/json");
            request2.addHeader("Authorization", authenticationToken);
            request2.setEntity(new StringEntity(new Gson().toJson(params)));

            HttpResponse response2 = client2.execute(request2);
            HttpEntity entity2 = response2.getEntity();
            String str2 = EntityUtils.toString(entity2, "UTF-8");
            //str2 = str2.substring(1);
            //str2 = str2.substring(0, str2.length() - 1);
            JSONParser parser2 = new JSONParser();
            Object obj2 = parser2.parse(str2);
            JSONObject json2 = (JSONObject)obj2;
            String uuid2 = (String)json2.get("uuid");
            
            result = "[{\"SELL\":\"YES\",\"uuid\":\""+uuid2+"\"}]";
            System.out.println("%%%%%%%:"+uuid2);
            System.out.println("%%%%%%%:"+result);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
}

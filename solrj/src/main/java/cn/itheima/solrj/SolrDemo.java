package cn.itheima.solrj;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.junit.Test;

/**
 * solr是根据id域执行索引的添加（更新）。
 * 先根据Id域执行搜索。搜索到执行更新；搜索不到执行添加。
 * @author xiqing
 *
 */
public class SolrDemo {
	
	/**
	 * 添加或更新索引
	 * @throws IOException 
	 * @throws SolrServerException 
	 */
	
	@Test
	
	public void addOrUpdateIndex() throws SolrServerException, IOException {
		//1.建立HttpSolrServer对象，连接solr服务
	   HttpSolrServer server = new HttpSolrServer("http://127.0.0.1:8082/solr");
		
	   //建立文档对象(SolrInputDocument)
	   SolrInputDocument doc = new SolrInputDocument();
	   doc.addField("id", "9582");
	   //doc.addField("name","solr is a good things");
	   doc.addField("name","lucene and solr are good things");
	   //使用HttpSolrServer对象，执行添加（更新）
	   server.add(doc);
	   
	   //提交
	   server.commit();
	}
	
	/***
	 * 根据id删除索引
	 * @throws IOException 
	 * @throws SolrServerException 
	 */
	@Test
	public void deleteIndex() throws SolrServerException, IOException {
		HttpSolrServer server = new HttpSolrServer("http://127.0.0.1:8082/solr");
	     server.deleteById("9582");
	     server.commit();
	}
	
	/***
	 * 根据条件删除索引
	 * 
	 */
	@Test
	public void deleteIndexByQuery() throws SolrServerException, IOException {
		HttpSolrServer server = new HttpSolrServer("http://127.0.0.1:8082/solr");
	     server.deleteByQuery("name:solr");
	     server.commit();
	}
	
	/**
	 * 查询索引
	 * @throws SolrServerException 
	 */
	@Test
	public void queryIndex() throws SolrServerException {
		HttpSolrServer server = new HttpSolrServer("http://127.0.0.1:8082/solr");
		 //1.先建立查询对象
		SolrQuery sq = new SolrQuery();
		 
		QueryResponse queryResponse = server.query(sq);
	     //获取结果集
		SolrDocumentList results = queryResponse.getResults();
	     System.out.println("实际搜索数量:"+results.getNumFound());
	
	     //遍历结果集
	     for (SolrDocument doc : results) {
	    	 System.out.println("-----------华丽丽的分割线----------");
				System.out.println("id域值："+doc.get("id"));
				System.out.println("name域值："+doc.get("name"));
		}
	     
	
	}
	
	
	@Test
	public void seniorQuery() throws Exception {
		//连接solr服务
		HttpSolrServer server = new HttpSolrServer("http://127.0.0.1:8082/solr");
	
		//建立查询对象
		SolrQuery sq = new SolrQuery();
		
		//设置条件
		//查询表达式q
		sq.setQuery("花儿朵朵");
		
		//过滤条件fq
		sq.setFilterQueries("product_price:[* TO 20]");
		//排序sort
		sq.setSort("product_price",ORDER.asc);
		
		//分页显示 start rows
		sq.setStart(0);
		sq.setRows(10);
		
		//域列表 fl
		sq.setFields("id","product_name","product_price","product_catalog");
		
		//默认搜索域 df
		sq.set("df", "product_name");
		
		//响应格式wt
		sq.set("wt", "json");
		
		//高亮显示 hl
		sq.setHighlight(true);//开启高亮显示
		sq.addHighlightField("product_name");//添加高亮显示的域
		sq.setHighlightSimplePre("<font color='red'>");// 设置html高亮显示标签的开始
		sq.setHighlightSimplePost("</font>");// 设置html高亮显示标签的结尾
		
		
		//分片统计 facet
		sq.setFacet(true);//开启分片统计
		sq.addFacetField("product_catalog_name");//添加分片统计域
	    
		
		QueryResponse queryResponse = server.query(sq);
		
		//获取结果集
		SolrDocumentList results = queryResponse.getResults();
		//取出高亮数据
		/**
		 *  "highlighting": {
			    "1": {
			      "product_name": [
			        "<font color='red'>花儿</font><font color='red'>朵朵</font>彩色金属门后挂 8钩免钉门背挂钩2066"
			      ]
			    },
		 */
		Map<String, Map<String, List<String>>> highlighting = queryResponse.getHighlighting();
		
		//获取分片统计数据
		List<FacetField> facetFields = queryResponse.getFacetFields();
		System.out.println("查看分片统计数据==========");
		
		for (FacetField f : facetFields) {
			System.out.println("分片统计域:"+f.getName());
		    System.out.println("有多少个分类:"+f.getValueCount());
		    
		    //查看组内信息
		    List<Count> values = f.getValues();
		    for (Count c : values) {
				System.out.println("分类名称:"+c.getName()+",满足条件的数量:"+c.getCount());
			 
		    }
		    
		}
		System.out.println("查看分片统计数据==========");
		
		
		
		
		
		
		
		System.out.println("实际上搜索到的数量:"+results.getNumFound());
	    for (SolrDocument doc : results) {
			System.out.println("-------华丽丽分割线------");
			
			//商品编号
			String pid=doc.get("id").toString();
			
			//商品名称
			String pname="";
			List<String> list = highlighting.get(pid).get("product_name");
			System.out.println(list);
			if(list!=null && list.size()>0) {
				pname=list.get(0);
			}else {
				pname=doc.get("product_name").toString();
			}
			
			//商品价格
			String pprice=doc.get("product_price").toString();
	       //商品分类id
			String pcatalog=doc.get("product_catalog").toString();
			
			System.out.println("商品Id:"+pid);
			System.out.println("商品名称:"+pname);
			System.out.println("商品价格:"+pprice);
			System.out.println("商品分类Id:"+pcatalog);
			
	    
	    }
	}
	
}

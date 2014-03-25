rest-compress
=============

Rest Compression Annotations

#Why?
Have you noticed that you're spending a significant amount of bandwidth and transfer time sending fluffy JSON or XML data from your RESTful Web services?  Wishing there were some way to reduce that, but without the high CPU overhead and increased server processing time from using GZIP? Look no further, here's your solution. :-)

Rest-compress is a library for the popular RestEasy JAX-RS framework.  Currently it adds support for the LZF algorithm, using Tatu Saloranta's excellent [Compress-LZF](https://github.com/ning/compress) library.
This generates an output compatible with the C LZF implementation.  In the future I hope to add support for additional compression methods (including XML/JSON specific compression formats).


#LZF?
LZF is a dictionary-based compression algorithm in the Lempel-Ziv (LZ) family, optimized for speed over compression ratio. The more popular DEFLATE/GZIP algorithm combines LZ with Huffman coding, but is significantly slower. In fact, within a data center or on a local network, GZIP is so slow that you will often reduce application performance slightly by using it, because the compression speed of <100 MB/s is less than the available bandwidth (100+ MB/s).

LZF is generally at least 2x as fast as GZIP, and still acheives significant (if not as high) compression while being fast enough to realize benefits within a data center.  For generic binary data, it is expected to reduce file size about 50%, for JSON data 75% reduction or more is not unreasonable.   Compression speed will actually increase as the data becomes more easily compressible, so it is a perfect match for XML and JSON. 

For very 'fluffy' XML/JSON, I have observed:
* 90% compression
* Using a *single* core of a Core i7-2620M, 800 MB/s compression, 1500 MB/s decompression, 536 MB/s total round-trip

More info on LZF: [C LZF documentation](http://oldhome.schmorp.de/marc/liblzf.html)


#How To Use:
To integrate in your projects:

* Build the rest-lzf-util module in Maven and add this dependency to your project:
```
<dependency>
    <groupId>rest-lzf</groupId>
    <artifactId>rest-lzf-util</artifactId>
    <version>0.1</version>
</dependency>
```

* Add import for the library to your JAX-RS server & client classes/interfaces:
```
import org.restcompress.provider.LZF;
```

* Add the @LZF annotation to your REST methods:
```
@GET
    @Path("/object")
    @LZF
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    /** Returns a sample serialized object */
    public KeyValue getObject();
```

* Enjoy!  RestEasy clients will automatically use LZF compression for Request respones and POST/PUT bodies, if both client and server advertise the capability.  If the client does not advertise the ability to accept the LZF encoding, the server will not use it. 


#How it works:
**Client-side:**
Client adds HTTP header "Accept-Encoding: lzf" to all requests and uses LZF encoding for PUT/POST request bodies, setting the 'Content-Encoding: lzf' header. 

**Server-side:**
If client request has 'Accept-Encoding: lzf' header set, the response will be LZF compressed and the 'Content-Encoding: lzf' header set.  If the client request body has header 'Content-Encoding: lzf' then the request body will be decompressed as LZF before further processing (deserialization, etc). 

**What this means:** 
You can use a client that is not LZF-aware and LZF simply won't be used.  This means you can easily test & debug REST APIs by the usual tools (curl, browser, etc). 

#Gotchas:
You may not be able to use both @GZIP and @LZF annotations on the same REST method.  They simply don't play well together, because of how the interceptors for GZip are implemented. For now, pick one method or the other and stick to it. 

#Testing:
You may notice that there aren't unit tests.  This is because the Interceptors only fire when actively running in a container and this project does not provide functionality separate from this.

To work around this, manual testing was done to verify that:
* Normal HTTP responses are returned if client doesn't advertise ability to accept LZF encoding 
* Clients built using a shared client/server REST interface successfully receive and use LZF responses from server
* Responses are compressed by LZF when sent to server with LZF specified
* Verified that original message is the same as compressed message after decompression.  (This is covered by the LZF compression library used.)
* Benchmarking on request/response times.

#Demo:
I've included a trivial REST demo app in the rest-demo-app module, which can be used as an example.  It is what I used in manual testing, as well.   To deploy, build it and drop the WAR in a JBoss Application Server instance. 


#Benchmarks:
Tested on a slow development VM, running curl on one VM to another VM, here are stats (reported as averages over 10,000 runs, with 1,000 runs to warm up).

**Benchmark1: serving generated "dummy" data
*Calling method: {hostname}:8080/rest/complex/10000** (JSON return)*

Compression | Avg pre-transfer time (s) | Average server processing time (s) | Average Transfer time (s) |   Avg Bytes  
------------|---------------------------|------------------------------------|---------------------------|--------------
 none       |      3.60506e-05          |           0.0239487289             |       0.0353947948        |   1766648.424   
 LZF        |      3.57284e-05          |           0.0258132831             |       0.0265397982        |    173025.0755 

*Total processing Rate (server time + transfer time):*

No Compression: **28.39** MB/s

LZF: Roughly **32.18 MB/s** (when uncompressed, file size should be same)

Overall, 25% reduction in transfer time (actually includes a lot of the encoding time). Yielding a 10% overall performance boost (given a fast connection, roughly 47 MB/s real speed). **Furthermore, there is a 89.8% reduction in bandwidth used.**   


**Benchmark 2: Serving statically generated version of the above ** 
*REST URL: {hostname}:8080/rest/static (all processing time should be for serialization/compression)*
*For GZIP, using: {hostname}:8080/rest/static/gzip*

Compression |Avg pre-transfer time (s) | Average server processing time (s) | Average Transfer time (s)|   Avg Bytes
------------|--------------------------|----------------------------------|------------------------|-------------
 none       |3.52369e-05 | 0.0021536384 | 0.0309138852 | 1842761.0 
 LZF        |3.33792e-05 | 0.0042147066 | 0.0223565097 |  174164.0 
 GZIP       |3.71741e-05 | 0.0150530411 | 0.0393193676 |   90949.0 

*Benchmark conditions: all benchmarks run by calling one development VM from another, over a fast network link (100+ MB/s real performance).  There was nothing to generate load on the test machines.*
*Machine configuration: JBoss EAP 6.1.1 application server, CPU: Intel Xeon E312xx (Sandy Bridge) @ 2.4 GHz, 2 cores, 4096 KB cache, 4 GB RAM*

#Future Plans:
The following additions are planned at some point, and are listed below in priority order so consumers are aware that they are already planned.  No ETA when they will be completed, however.

1. Add Arquillian testing to replace manual testing above
2. Investigate some way to allow GZIP & LZF compression annotations to play nicely together, and add support for multiple encoding options
3. Add support for JAX-RS 2.0 interceptors (RestEasy 3.x and other JAX-RS libraries)
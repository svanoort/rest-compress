rest-compress
=============

Fast HTTP Compression For RestEasy

#Why?
Have you noticed that you're spending a significant amount of bandwidth and transfer time sending fluffy JSON or XML data from your Java RESTful Web services?  Wishing there were some way to reduce that, but without GZIP's high CPU overhead slowing things down? Look no further, here's your solution. :-)

Rest-compress extends the popular RestEasy JAX-RS framework, by adding support for *fast* compression.  All you have to do is add an annotation to your methods: it's as easy as GZIP, but fast enough to use within the data center or to the cloud.

The initial version uses the LZF algorithm, based on Tatu Saloranta's excellent [Compress-LZF](https://github.com/ning/compress) library.  For compatibility with other libraries, it generates an [output format](https://github.com/ning/compress/wiki/LZFFormat) compatible with the C LZF implementation.  There are LZF libraries [for all the popular languages](https://github.com/ning/compress/#interoperability).  So, you can run a Java server, but easily add support in your Python or Ruby front-end!

In the future I hope to add support for additional compression methods (including XML/JSON specific compression formats).


#LZF?
LZF is a speed-optimized compression algorithm in the Lempel-Ziv (LZ) family, optimized for speed over compression ratio. The more popular DEFLATE/GZIP algorithm combines LZ with Huffman coding for better compression, but is significantly slower.  **With GZIP, you spend a lot of CPU time to get your compression, and on a LAN connection it will often reduce application performance.**

**LZF vs. GZIP for JSON/XML** (TPS is MB/s, size % is ratio of compressed to uncompressed data)
<img src="https://raw.githubusercontent.com/svanoort/rest-compress/wiki/wiki/result.jpg" />

###There are other fast compression algorithms out there, so why LZF?
* It's quite **fast.** On my laptop's Core i7-2620M CPU, I've seen compression exceed 800 MB/s for particularly 'fluffy' JSON data, and reduce size by 90%
* **Pure Java, no JNI.** Portable across all platforms, even on Android phones.
* Good **cross-language** libraries, meaning it's not hard for people to write LZF-enabled REST clients in any language
* **Not patent-encumbered** (like LZW and LZO)
* It's familiar; I did low-level work with H2's LZF implementation, and it's easiest to start out with something familiar

More info on LZF: [C LZF documentation](http://oldhome.schmorp.de/marc/liblzf.html)


#How To Use:
To integrate in your projects:

* Build the rest-compress-util module in Maven and add this dependency to your project:
```
<dependency>
    <groupId>com.codeablereason.restcompress.provider</groupId>
    <artifactId>rest-compress-lib</artifactId>
    <version>0.5</version>
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

* Enjoy!  The server will automatically send LZF-compressed REST responses if the client advertises the ability.  Clients will automatically decode use LZF compression for POST/PUT bodies.  If the client does not advertise the ability to accept the LZF encoding, the server will not use it.


#Benchmarks:
Here are some benchmarks to whet the appetite.  Methodology and machine specs are at the bottom.


##Network Benchmark 1: Serving Static Content

*Demo App REST URL: {hostname}:8080/rest/static (all processing time should be for serialization & compression)*
*For GZIP, using: {hostname}:8080/rest/static/gzip*

Compression | Request Time (s) | Avg Data Size (kB)
------------|------------------|-------------------------:
 none       |     0.03310      |     1842761.0
 LZF        |     0.02660      |      174164.0
 GZIP       |     0.05441      |       90949.0

As you can see, LZF reduces file size by 90.5% (not as good as GZIP's 95.1%) but is considerably faster.  Where GZIP's CPU-expensive compression reduces performance by 39.2%, **LZF improves overall REST response time by 24.4% (!)** despite being on a very fast network.


##Network Benchmark 2: Serving Generated "dummy" data

*Calling demo app method: {hostname}:8080/rest/complex/10000** (JSON return)*

Compression | Request Time (s) | Avg Data Size (kB)
------------|------------------|-------------------------:
 none       |      0.05938     |           1766648.424
 LZF        |      0.05239     |            173025.076

*Request time is measured from sending request until response fully received.*

Even with nontrivial processing to generate data, this yields an 89.8% reduction in bandwidth, giving a 10% performance boost.  This is despite being on fast LAN connection (10 GBit shared between VMs).


#Dedicated LZF vs. GZIP Compression Benchmarks
This gives some idea of the compression speed, as a comparison with GZIP, and for estimating when LZF is advantageous.  It will vary with the hardware used, of course.
*Your mileage will vary.* Full benchmark results are [here](https://github.com/svanoort/rest-compress/wiki/Benchmark:-REST-Corpus-LZF-GZIP-Round-Trip-Benchmark).

Compression | Avg Round-Trip Speed (MB/s) | Compression Ratio
------------|-----------------------------|-------------------------:
 LZF        |      174.596                |           29.803%
 GZIP       |       30.851                |           18.172%

**Round-trip speed is the total rate at which data can be compressed and decompressed.  It is reported as the harmonic mean of the speeds for all files in the corpus.**
**Harmonic mean is used here instead of arithmetic mean, because it better represents throughput, as it does not disproportionately weight the maximum values.**

As you can see, LZF is about 5x faster than GZIP, at the cost of a somewhat reduced compression. For our hardware, we see that:
- Neither compression generates benefits when the real available bandwidth is >122 MB/s *(fraction_reduction_in_data * bandwidth < speed_of_decompression)*
- Full derivation of the equation above (and math for deciding which algorithm to use) is [here](../wiki/Comparing-Speed-of-Different-Compression-Algorithms)
- GZIP generates no benefits if the network exceeds 25.2 MB/s (compression & decompression takes longer than it saves)
- In the benchmark below, LZF beats GZIP if the bandwidth exceeds 4.36 MB/s.  On faster CPUs, this increases some (for my ~50% faster dev laptop, it is 6.40 MB/s)
- In general, LZF beats GZIP on data throughput to host when bandwidth exceeds (ratio_LZF - ratio_GZIP) / ( (1/speed_GZIP) - (1/speed_LZF)) )
- Note that gigabit ethernet's maximum speed is 128 MB/s on an uncongested link, and real world performance will be quite a bit less


#How it works:
* **Client-side:**
Client adds HTTP header "Accept-Encoding: lzf" to all REST requests and uses LZF encoding for PUT/POST request bodies, setting the 'Content-Encoding: lzf' header.

* **Server-side:**
If client request has 'Accept-Encoding: lzf' header set, the response will be LZF compressed and the 'Content-Encoding: lzf' header set.  If the client request body has header 'Content-Encoding: lzf' then the request body will be decompressed as LZF before further processing (deserialization, etc).

* **What this means:**
You can use a client that is not LZF-aware and LZF simply won't be used.  This means you can easily test & debug REST APIs by the usual tools (curl, browser, etc).
Thanks to the magic of HTTP Headers, you can call the RESTful services from non-Java clients without LZF compression, and then later build this into them as you choose.


#Gotchas:
You may not be able to use both @GZIP and @LZF annotations on the same REST method.  They simply don't play well together, because of how the interceptors for GZip are implemented. For now, pick one method or the other and stick to it.


#Demo:
I've included a trivial REST demo app in the rest-demo-app module, which can be used as an example.  It is what I used in manual testing, as well.   To deploy, build it and drop the WAR in a JBoss Application Server 7 instance (in the deployments folder).



#Testing:
You may notice that there aren't unit tests.  This is because the Interceptors only fire when actively running in a container and this project does not provide functionality separate from this.

To work around this, manual testing was done (using the demo app) to verify that:
* Normal HTTP responses are returned if client doesn't advertise ability to accept LZF encoding
* Clients built using a shared client/server REST interface successfully receive and use LZF responses from server
* Responses are compressed by LZF when sent to server with "Accept-Encoding: lzf" header
* Verified that original message is the same as compressed message after decompression.  (This is covered by the LZF compression library used.)
* Benchmarking on request/response times.



#Future Plans:
The following additions are planned at some point, and are listed below in priority order so consumers are aware that they are already planned.  No ETA when they will be completed, however.

1. Modify implementation to allow both GZIP & LZF support (and others), to facilitate migration
2. Add Arquillian testing to replace manual testing above
3. Add support for JAX-RS 2.0 interceptors (RestEasy 3.x and other JAX-RS libraries)

##Benchmark Methodology
- *I make no guarantees that the benchmarks are perfect, but I've tried to be as careful and scientific as possible and remove sources of external noise here.*
- There were no processes running to generate load on the VM or VMs used

###Network Tests:
- For tests of network performance, this was tested using the python script included under the rest-demo-app resources folder.
- This test runs single REST requests by PyCurl from one VM to another VM, doing 1000 runs to warm up, and reporting arithmetic averages over 10,000 runs.
- VM is on a blade server connected by 10 GBit link to LAN (shared across guest VMs)

###LZF Tests:
- For benchmarks of LZF performance, the test used a [jvm-compressor-benchmark](https://github.com/svanoort/jvm-compressor-benchmark) fork
- To provide a representative sample of general REST response content, I gathered a corpus of JSON & XML data from 4 different public REST APIs
- The test data is included, under the /testdata/rest folder, along with the shell script used to fetch it (which lists sources)
- The benchmark script is: run-lzf-rest-round.sh
- Test is using only a single core of the machine


##Benchmark Machine:
- Development VM, 2 cores, no load, 4 GB of RAM allocated from host
- Host & Guest OS: Red Hat Enterprise Linux 6
- For network testing, using JBoss EAP 6.1.1
- Anecdotally, the VMs are quite a bit slower than my dev laptop (equivalent benchmarks are about 30-50% slower)
- Java: java version "1.7.0_45", OpenJDK Runtime Environment (rhel-2.4.3.3.el6-x86_64 u45-b15)
- OpenJDK 64-Bit Server VM (build 24.45-b08, mixed mode)

**Host Hardware:**
- Host: CPU: 2.40 GHz E5-2665/115W 8C/20MB Cache/DDR3 1600MHz
- Host: RAM DIMMs: 16GB DDR3-1600-MHz RDIMM/PC3-12800/dual rank/1.35v
- Server: UCS B200 M3 Blade Server
- Cisco UCS VIC 1240
import pycurl
""" Testbed for pycurl experimentation scripts """

warmup_runs = 1000
benchmark_runs = 10000
print_intermediate = False 
url = 'localhost:8080/rest/complex/10000'


curl = pycurl.Curl()
curl.setopt(curl.URL,url)
curl.setopt(pycurl.WRITEFUNCTION, lambda x: None) #Do not print to console at all
curl.setopt(pycurl.ENCODING,'lzf')
#curl.setopt(pycurl.ENCODING,'gzip')

# Source: http://pycurl.sourceforge.net/doc/curlobject.html
# http://curl.haxx.se/libcurl/c/curl_easy_getinfo.html -- this is the info parameters, used for timing, etc
info_fetch = {'response_code':pycurl.RESPONSE_CODE,
    'pretransfer_time':pycurl.PRETRANSFER_TIME,
    'starttransfer_time':pycurl.STARTTRANSFER_TIME,
    'size_download':pycurl.SIZE_DOWNLOAD,
    'total_time':pycurl.TOTAL_TIME
}

print 'Warmup: started'
for x in xrange(0, warmup_runs):
    curl.perform()
print 'Warmup: finished'

bytes = dict()
speed = dict()
time_pre = dict()
time_server = dict() 
time_xfer = dict()



print 'Benchmark: starting'
for x in xrange(0, benchmark_runs):
    curl.perform()
    if curl.getinfo(pycurl.RESPONSE_CODE) != 200:
        raise Exception('Error: failed call to service!')        

    time_pretransfer = curl.getinfo(pycurl.PRETRANSFER_TIME) #Time to negotiate connection, before server starts response negotiation
    time_starttransfer = curl.getinfo(pycurl.STARTTRANSFER_TIME) #Pre-transfer time until server has generated response, just before first byte sent
    time_total = curl.getinfo(pycurl.TOTAL_TIME) #Download included
    
    time_xfer[x] = time_total - time_starttransfer
    time_server[x] = time_starttransfer - time_pretransfer
    time_pre[x] = time_pretransfer

    bytes[x] = curl.getinfo(pycurl.SIZE_DOWNLOAD) #bytes
    speed[x] = curl.getinfo(pycurl.SPEED_DOWNLOAD) #bytes/sec
    
    if print_intermediate:
        print 'Bytes: {size}, speed (MB/s) {speed}'.format(size=bytes[x],speed=speed[x]/(1024*1024))
        print 'Pre-transfer, server processing, and transfer times: {pre}/{server}/{transfer}'.format(pre=time_pretransfer,server=time_server[x],transfer=time_xfer[x])

#print info
curl.close()
print 'Benchmark: ending'

print 'Average bytes {bytes}, average transfer speed (MB/s): {speed}'.format(
    bytes=sum(bytes.values())/benchmark_runs,
    speed=sum(speed.values())/(benchmark_runs*1024*1024)
)

print 'Avg pre/server/xfer time (s) {pre}/{server}/{transfer}'.format(
    pre=sum(time_pre.values())/benchmark_runs,
    server=sum(time_server.values())/benchmark_runs,
    transfer=sum(time_xfer.values())/benchmark_runs
)
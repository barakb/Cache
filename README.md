[![Build Status](https://travis-ci.org/barakb/Cache.svg?branch=master)](https://travis-ci.org/barakb/Cache) 

Cache -- An LRU Java Cache
==========================
## DESCRIPTION
Fast Flexible efficient in memory LRU Java cache
This repository has the sources to [my post](http://bar-orion.blogspot.com/2012/02/fast-flexible-efficient-in-memory-java.html).
You may copy or use any part of the code freely.
Barak.
Sample Usage:

     Cache<String, byte[]> fileContentCache =
                new Cache<String, byte[]>(new Compute<String, byte[]>() {
                    @Override
                    public byte[] compute(String key) throws Exception {
                        return readFileContent(new File(key));
                    }
                }, 10);

See _examples_ directory.
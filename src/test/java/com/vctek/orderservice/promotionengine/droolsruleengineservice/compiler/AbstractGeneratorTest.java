package com.vctek.orderservice.promotionengine.droolsruleengineservice.compiler;




import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

public abstract class AbstractGeneratorTest
{
    protected String getResourceAsString(final String name) throws IOException
    {
        try
        {
            InputStream inputStream = getClass().getResourceAsStream(name);
            return IOUtils.toString(inputStream, "UTF-8");
        } catch (IOException e ){
            throw new RuntimeException(e.getMessage());
        }
    }
}

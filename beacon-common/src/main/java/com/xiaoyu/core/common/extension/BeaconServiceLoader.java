/*
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */
package com.xiaoyu.core.common.extension;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.ServiceConfigurationError;

/**
 * @author hongyu
 * @date 2018-05
 * @description 取之于java.util.ServiceLoader,这里复制重写,对parseLine和iterator方法进行稍微改造来适应特定的需要
 * @param <S>
 */
public final class BeaconServiceLoader<S> implements Iterable<S> {

    private static final String PREFIX = "META-INF/services/";

    // The class or interface representing the service being loaded
    private final Class<S> service;

    // The class loader used to locate, load, and instantiate providers
    private final ClassLoader loader;

    // The access control context taken when the ServiceLoader is created
    private final AccessControlContext acc;

    // Cached providers, in instantiation order
    private LinkedHashMap<String, S> providers = new LinkedHashMap<>();

    // The current lazy-lookup iterator
    private LazyIterator lookupIterator;

    /**
     * 作为ExtenderHolder的key
     */
    private String protocolName;

    public String getProtocolName() {
        return protocolName;
    }

    /**
     * Clear this loader's provider cache so that all providers will be
     * reloaded.
     * <p>
     * After invoking this method, subsequent invocations of the {@link
     * #iterator() iterator} method will lazily look up and instantiate
     * providers from scratch, just as is done by a newly-created loader.
     * <p>
     * This method is intended for use in situations in which new providers
     * can be installed into a running Java virtual machine.
     */
    public void reload() {
        providers.clear();
        lookupIterator = new LazyIterator(service, loader);
    }

    private BeaconServiceLoader(Class<S> svc, ClassLoader cl) {
        service = Objects.requireNonNull(svc, "Service interface cannot be null");
        loader = (cl == null) ? ClassLoader.getSystemClassLoader() : cl;
        acc = (System.getSecurityManager() != null) ? AccessController.getContext() : null;
        reload();
    }

    private static void fail(Class<?> service, String msg, Throwable cause)
            throws ServiceConfigurationError {
        throw new ServiceConfigurationError(service.getName() + ": " + msg,
                cause);
    }

    private static void fail(Class<?> service, String msg)
            throws ServiceConfigurationError {
        throw new ServiceConfigurationError(service.getName() + ": " + msg);
    }

    private static void fail(Class<?> service, URL u, int line, String msg)
            throws ServiceConfigurationError {
        fail(service, u + ":" + line + ": " + msg);
    }

    // Parse a single line from the given configuration file, adding the name
    // on the line to the names list.
    //
    private int parseLine(Class<?> service, URL u, BufferedReader r, int lc,
            Map<String, String> names)
            throws IOException, ServiceConfigurationError {
        String ln = r.readLine();
        if (ln == null) {
            return -1;
        }
        int ci = ln.indexOf('#');
        if (ci >= 0)
            ln = ln.substring(0, ci);
        ln = ln.trim();
        int n = ln.length();
        if (n != 0) {
            if ((ln.indexOf(' ') >= 0) || (ln.indexOf('\t') >= 0))
                fail(service, u, lc, "Illegal configuration-file syntax");
            int cp = ln.codePointAt(0);
            if (!Character.isJavaIdentifierStart(cp))
                fail(service, u, lc, "Illegal provider-class name: " + ln);
            for (int i = Character.charCount(cp); i < n; i += Character.charCount(cp)) {
                cp = ln.codePointAt(i);
                if (!Character.isJavaIdentifierPart(cp) && (cp != '.'))
                    fail(service, u, lc, "Illegal provider-class name: " + ln);
            }
            //
            protocolName = ln.substring(0, ln.indexOf("."));
            if (!providers.containsKey(ln) && !names.containsKey(protocolName)) {
                // xiaoyu:取第一个.之后的
                ln = ln.substring(ln.indexOf(".") + 1);
                names.put(protocolName, ln);
            }
        }
        return lc + 1;
    }

    // Parse the content of the given URL as a provider-configuration file.
    //
    // @param service
    // The service type for which providers are being sought;
    // used to construct error detail strings
    //
    // @param u
    // The URL naming the configuration file to be parsed
    //
    // @return A (possibly empty) iterator that will yield the provider-class
    // names in the given configuration file that are not yet members
    // of the returned set
    //
    // @throws ServiceConfigurationError
    // If an I/O error occurs while reading from the given URL, or
    // if a configuration-file format error is detected
    //
    private Iterator<Entry<String, String>> parse(Class<?> service, URL u)
            throws ServiceConfigurationError {
        InputStream in = null;
        BufferedReader r = null;
        Map<String, String> names = new HashMap<>();
        try {
            in = u.openStream();
            r = new BufferedReader(new InputStreamReader(in, "utf-8"));
            int lc = 1;
            while ((lc = parseLine(service, u, r, lc, names)) >= 0)
                ;
        } catch (IOException x) {
            fail(service, "Error reading configuration file", x);
        } finally {
            try {
                if (r != null)
                    r.close();
                if (in != null)
                    in.close();
            } catch (IOException y) {
                fail(service, "Error closing configuration file", y);
            }
        }
        return names.entrySet().iterator();
    }

    // Private inner class implementing fully-lazy provider lookup
    //
    private class LazyIterator
            implements Iterator<S> {

        Class<S> service;
        ClassLoader loader;
        Enumeration<URL> configs = null;
        Iterator<Entry<String, String>> pending = null;
        Entry<String, String> nextName = null;

        private LazyIterator(Class<S> service, ClassLoader loader) {
            this.service = service;
            this.loader = loader;
        }

        private boolean hasNextService() {
            if (nextName != null) {
                return true;
            }
            if (configs == null) {
                try {
                    String fullName = PREFIX + service.getName();
                    if (loader == null)
                        configs = ClassLoader.getSystemResources(fullName);
                    else
                        configs = loader.getResources(fullName);
                } catch (IOException x) {
                    fail(service, "Error locating configuration files", x);
                }
            }
            while ((pending == null) || !pending.hasNext()) {
                if (!configs.hasMoreElements()) {
                    return false;
                }
                pending = parse(service, configs.nextElement());
            }
            nextName = pending.next();
            return true;
        }

        private S nextService() {
            if (!hasNextService())
                throw new NoSuchElementException();
            String cn = nextName.getValue();
            protocolName = nextName.getKey();
            nextName = null;
            Class<?> c = null;
            try {
                c = Class.forName(cn, false, loader);
            } catch (ClassNotFoundException x) {
                fail(service,
                        "Provider " + cn + " not found");
            }
            if (!service.isAssignableFrom(c)) {
                fail(service,
                        "Provider " + cn + " not a subtype");
            }
            try {
                @SuppressWarnings("null")
                S p = service.cast(c.newInstance());
                providers.put(cn, p);
                return p;
            } catch (Throwable x) {
                fail(service,
                        "Provider " + cn + " could not be instantiated",
                        x);
            }
            throw new Error(); // This cannot happen
        }

        @Override
        public boolean hasNext() {
            if (acc == null) {
                return hasNextService();
            } else {
                PrivilegedAction<Boolean> action = new PrivilegedAction<Boolean>() {
                    @Override
                    public Boolean run() {
                        return hasNextService();
                    }
                };
                return AccessController.doPrivileged(action, acc);
            }
        }

        @Override
        public S next() {
            if (acc == null) {
                return nextService();
            } else {
                PrivilegedAction<S> action = new PrivilegedAction<S>() {
                    @Override
                    public S run() {
                        return nextService();
                    }
                };
                return AccessController.doPrivileged(action, acc);
            }
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

    }

    /**
     * Lazily loads the available providers of this loader's service.
     * <p>
     * The iterator returned by this method first yields all of the
     * elements of the provider cache, in instantiation order. It then lazily
     * loads and instantiates any remaining providers, adding each one to the
     * cache in turn.
     * <p>
     * To achieve laziness the actual work of parsing the available
     * provider-configuration files and instantiating providers must be done by
     * the iterator itself. Its {@link java.util.Iterator#hasNext hasNext} and
     * {@link java.util.Iterator#next next} methods can therefore throw a
     * {@link ServiceConfigurationError} if a provider-configuration file
     * violates the specified format, or if it names a provider class that
     * cannot be found and instantiated, or if the result of instantiating the
     * class is not assignable to the service type, or if any other kind of
     * exception or error is thrown as the next provider is located and
     * instantiated. To write robust code it is only necessary to catch {@link
     * ServiceConfigurationError} when using a service iterator.
     * <p>
     * If such an error is thrown then subsequent invocations of the
     * iterator will make a best effort to locate and instantiate the next
     * available provider, but in general such recovery cannot be guaranteed.
     * <blockquote style="font-size: smaller; line-height: 1.2"><span
     * style="padding-right: 1em; font-weight: bold">Design Note</span>
     * Throwing an error in these cases may seem extreme. The rationale for
     * this behavior is that a malformed provider-configuration file, like a
     * malformed class file, indicates a serious problem with the way the Java
     * virtual machine is configured or is being used. As such it is
     * preferable to throw an error rather than try to recover or, even worse,
     * fail silently.</blockquote>
     * <p>
     * The iterator returned by this method does not support removal.
     * Invoking its {@link java.util.Iterator#remove() remove} method will
     * cause an {@link UnsupportedOperationException} to be thrown.
     *
     * @implNote When adding providers to the cache, the {@link #iterator
     *           Iterator} processes resources in the order that the {@link
     *           java.lang.ClassLoader#getResources(java.lang.String)
     *           ClassLoader.getResources(String)} method finds the service
     *           configuration
     *           files.
     * @return An iterator that lazily loads providers for this loader's
     *         service
     */
    @Override
    public Iterator<S> iterator() {
        return new Iterator<S>() {

            Iterator<Map.Entry<String, S>> knownProviders = providers.entrySet().iterator();

            @Override
            public boolean hasNext() {
                if (knownProviders.hasNext())
                    return true;
                return lookupIterator.hasNext();
            }

            @Override
            public S next() {
                if (knownProviders.hasNext())
                    return knownProviders.next().getValue();
                return lookupIterator.next();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }

        };
    }

    /**
     * Creates a new service loader for the given service type and class
     * loader.
     *
     * @param <S>
     *            the class of the service type
     * @param service
     *            The interface or abstract class representing the service
     * @param loader
     *            The class loader to be used to load provider-configuration files
     *            and provider classes, or <tt>null</tt> if the system class
     *            loader (or, failing that, the bootstrap class loader) is to be
     *            used
     * @return A new service loader
     */
    public static <S> BeaconServiceLoader<S> load(Class<S> service,
            ClassLoader loader) {
        return new BeaconServiceLoader<>(service, loader);
    }

    /**
     * Creates a new service loader for the given service type, using the
     * current thread's {@linkplain java.lang.Thread#getContextClassLoader
     * context class loader}.
     * <p>
     * An invocation of this convenience method of the form
     * <blockquote>
     * 
     * <pre>
     * ServiceLoader.load(<i>service</i>)
     * </pre>
     * 
     * </blockquote>
     * is equivalent to
     * <blockquote>
     * 
     * <pre>
     * ServiceLoader.load(<i>service</i>,
     *                    Thread.currentThread().getContextClassLoader())
     * </pre>
     * 
     * </blockquote>
     *
     * @param <S>
     *            the class of the service type
     * @param service
     *            The interface or abstract class representing the service
     * @return A new service loader
     */
    public static <S> BeaconServiceLoader<S> load(Class<S> service) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        return BeaconServiceLoader.load(service, cl);
    }

    /**
     * Creates a new service loader for the given service type, using the
     * extension class loader.
     * <p>
     * This convenience method simply locates the extension class loader,
     * call it <tt><i>extClassLoader</i></tt>, and then returns
     * <blockquote>
     * 
     * <pre>
     * ServiceLoader.load(<i>service</i>, <i>extClassLoader</i>)
     * </pre>
     * 
     * </blockquote>
     * <p>
     * If the extension class loader cannot be found then the system class
     * loader is used; if there is no system class loader then the bootstrap
     * class loader is used.
     * <p>
     * This method is intended for use when only installed providers are
     * desired. The resulting service will only find and load providers that
     * have been installed into the current Java virtual machine; providers on
     * the application's class path will be ignored.
     *
     * @param <S>
     *            the class of the service type
     * @param service
     *            The interface or abstract class representing the service
     * @return A new service loader
     */
    public static <S> BeaconServiceLoader<S> loadInstalled(Class<S> service) {
        ClassLoader cl = ClassLoader.getSystemClassLoader();
        ClassLoader prev = null;
        while (cl != null) {
            prev = cl;
            cl = cl.getParent();
        }
        return BeaconServiceLoader.load(service, prev);
    }

    /**
     * Returns a string describing this service.
     *
     * @return A descriptive string
     */
    @Override
    public String toString() {
        return "java.util.ServiceLoader[" + service.getName() + "]";
    }

}

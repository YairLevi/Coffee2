package org.levi.coffee


import dev.webview.Webview
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import org.levi.coffee.internal.CodeGenerator
import org.levi.coffee.internal.MethodBinder
import org.levi.coffee.internal.util.FileUtil
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*
import java.util.function.Consumer
import kotlin.system.exitProcess

class Window(val dev: Boolean = true) {
    private val log: Logger = LoggerFactory.getLogger(this::class.java)

    private val _webview: Webview = Webview(true)
    private val _beforeStartCallbacks: MutableList<Runnable> = ArrayList()
    private val _onCloseCallbacks: MutableList<Runnable> = ArrayList()
    private val _bindObjects = ArrayList<Any>()
    private var _url: String = ""

    init {
        setSize(800, 600)
    }

    fun setURL(url: String) {
        _url = url
    }

    fun setHTMLFromResource(resourcePath: String) {
        val resource = ClassLoader.getSystemClassLoader().getResource(resourcePath)
        if (resource == null) {
            log.error("Resource at $resourcePath was not found.")
            exitProcess(1)
        }
        _url = resource.toURI().toString()
    }

    fun setRawHTMLFromFile(path: String, isBase64: Boolean = false) {
        FileUtil.validateFileExists(path)
        val content = FileUtil.readText(path)
        setRawHTML(content, isBase64)
    }

    fun setRawHTML(html: String, isBase64: Boolean = false) {
        _url = "data:text/html"
        if (isBase64) {
            _url += ";base64,${Base64.getEncoder().encodeToString(html.toByteArray())}"
        } else {
            _url += ",$html"
        }
    }

    fun setTitle(title: String) {
        _webview.setTitle(title)
    }

    fun setSize(width: Int, height: Int) {
        _webview.setSize(width, height)
    }

    fun setMinSize(minWidth: Int, minHeight: Int) {
        _webview.setMinSize(minWidth, minHeight)
    }

    fun setMaxSize(maxWidth: Int, maxHeight: Int) {
        _webview.setMaxSize(maxWidth, maxHeight)
    }

    fun setFixedSize(fixedWidth: Int, fixedHeight: Int) {
        _webview.setFixedSize(fixedWidth, fixedHeight)
    }

    fun bind(vararg objects: Any) {
        for (o in objects) {
            _bindObjects.add(o)
        }
    }

    fun addBeforeStartCallback(r: Runnable) {
        _beforeStartCallbacks.add(r)
    }

    fun addOnCloseCallback(r: Runnable) {
        _onCloseCallbacks.add(r)
    }


    fun run() {
        if (dev) {
            val cg = CodeGenerator()
            cg.generateTypes(*_bindObjects.toTypedArray())
            cg.generateFunctions(*_bindObjects.toTypedArray())
            cg.generateEventsAPI()
        }

        var server: NettyApplicationEngine? = null
        if (!dev) {
            val prodPort = 4567
            server = embeddedServer(Netty, port = prodPort, host = "localhost") {
                routing {
                    staticResources("/", "dist") {
                        default("index.html")
                        preCompressed(CompressedFileType.GZIP)
                    }
                }
            }
            server.start()
            _url = "http://localhost:$prodPort"
        }

        MethodBinder.bind(_webview, *_bindObjects.toTypedArray())
        Ipc.setWebview(_webview)
        _beforeStartCallbacks.forEach(Consumer { it.run() })

        _webview.loadURL(_url)
        _webview.run()

        _onCloseCallbacks.forEach(Consumer { it.run() })
        _webview.close()

        server?.stop()

    }
}
import io.reactivex.rxjava3.annotations.NonNull
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.subscribeBy
import java.io.File
import java.io.FileNotFoundException
import kotlin.math.pow
import kotlin.math.roundToInt

fun main(args: Array<String>) {

    exampleOf("never") {
        val observable = Observable
            .never<Any>()
            .doOnSubscribe { println("Subscribed.") }
            .doOnDispose { println("Disposed.") }


        val subscribeBy = observable.subscribeBy(
            onNext = { println(it) },
            onComplete = { println("Completed") }
        )
    }

    exampleOf("just") {
        val observable = Observable.just(1, 2, 3)
        observable.subscribe { println(it) }
    }

    exampleOf("justList") {
        val observable = Observable.just(listOf(1, 2, 3))
        observable.subscribe { println(it) }
    }

    exampleOf("fromIterable") {
        val observable: Observable<Int> =
            Observable.fromIterable(listOf(1, 2, 3))
        observable.subscribe { println(it) }
    }

    exampleOf("empty") {
        val observable = Observable.empty<Unit>()
        observable.subscribeBy(
            onNext = { println(it) },
            onComplete = { println("Completed") }
        )
    }

    exampleOf("range") {
        val observable: @NonNull Observable<Int>? = Observable
            .range(1, 10)
            .map { calculateFib(it.toDouble()) }

        observable?.subscribe {
            val n = it.toDouble()
            val fibonacci = calculateFib(n)
            println(it)
        }
    }

    exampleOf("CompositeDisposable") {
        val subscriptions = CompositeDisposable()
        val disposable = Observable.just("A", "B", "C").subscribe { println(it) }
        subscriptions.add(disposable)
        subscriptions.dispose()
    }

    exampleOf("create") {
        Observable
            .create<String> {
                it.onNext("1")
//                it.onComplete()
                it.onNext("2")
            }
            .doOnDispose { println("Disposed") }
            .subscribeBy(
                onNext = { println(it) },
                onComplete = { println("Completed") },
                onError = { println(it) }
            )
            .dispose()

    }

    exampleOf("defer") {
        val disposables = CompositeDisposable()
        var flag = false

        println(Thread.currentThread())

        val factory = Observable.defer {
            println(Thread.currentThread())
            flag = !flag
            if (flag) {
                Observable.just(1, 2, 3)
            } else {
                Observable.just(4, 5, 6)
            }
        }

        for (i in 0..3) {
            disposables.add(
                factory.subscribe {
                    println(it)
                }
            )
        }

        disposables.dispose()
    }

    exampleOf("Single") {
        val subscriptions = CompositeDisposable()

        val observer = loadText("LoremIpsum.txt")
            .subscribeBy(
                onSuccess = { println(it) },
                onError = { println("Error, $it") }
            )

        subscriptions.add(observer)
    }

}

private fun loadText(filename: String): Single<String> {
    return Single.create create@{ emitter ->
        val file = File(filename)

        if (!file.exists()) {
            emitter.onError(FileNotFoundException("Can’t find $filename"))
            return@create
        }

        val contents = file.readText(Charsets.UTF_8)

        emitter.onSuccess(contents)
    }
}

private fun calculateFib(n: Double): Int {
    return ((1.61803.pow(n) - 0.61803.pow(n)) / 2.23606).roundToInt()
}


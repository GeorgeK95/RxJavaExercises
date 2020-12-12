import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.subjects.PublishSubject

var start = 0

private fun getStartNumber(): Int {
    start++
    return start
}

fun main() {

    exampleOf("") {
        val numbers = Observable.create<Int> { emitter ->
            val start = getStartNumber()
            emitter.onNext(start)
            emitter.onNext(start + 1)
            emitter.onNext(start + 2)
            emitter.onComplete()
        }

        numbers
            .subscribeBy(
                onNext = { println("element1 [$it]") },
                onComplete = { println(("-------------")) }
            )
        numbers
            .subscribeBy(
            onNext = { println("element2 [$it]") },
            onComplete = { println(("-------------")) }
        )
        numbers.subscribeBy(
            onNext = { println("element3 [$it]") },
            onComplete = { println(("-------------")) }
        )

    }

    exampleOf("distinctUntilChangedPredicate") {
        val subscriptions = CompositeDisposable()

        Observable.just("ABC", "BCD", "CDE", "FGH", "IJK", "JKL", "LMN")
            .distinctUntilChanged { first, second -> second.any { it in first } }
            .subscribe { println(it) }
            .addTo(subscriptions)
    }

    exampleOf("distinctUntilChanged") {
        val subscriptions = CompositeDisposable()

        Observable.just("Dog", "Cat", "Cat", "Dog")
            .distinctUntilChanged()
            .subscribe { println(it) }
            .addTo(subscriptions)
    }

    exampleOf("takeUtil") {
        val subscriptions = CompositeDisposable()

        val subject = PublishSubject.create<String>()
        val trigger = PublishSubject.create<String>()

        subject
            .takeUntil(trigger)
            .subscribe { println(it) }
            .addTo(subscriptions)

        subject.onNext("A")
        subject.onNext("B")

        trigger.onNext("X")

        subject.onNext("C")
    }

    exampleOf("takeWhile") {
        val subscriptions = CompositeDisposable()

        Observable.fromIterable(listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 1))
            .takeWhile { number -> number < 5 }
            .subscribe { println(it) }
            .addTo(subscriptions)
    }

    exampleOf("take") {
        val subscriptions = CompositeDisposable()

        Observable.just(1, 2, 3, 4, 5, 6)
            .take(3)
            .subscribe { println(it) }
            .addTo(subscriptions)
    }

    exampleOf("skipUntil") {
        val subscriptions = CompositeDisposable()

        val subject = PublishSubject.create<String>()
        val trigger = PublishSubject.create<String>()

        subject
            .skipUntil(trigger)
            .subscribe { println(it) }
            .addTo(subscriptions)

        subject.onNext("A")
        subject.onNext("B")

        trigger.onNext("X")

        subject.onNext("C")
    }

    exampleOf("skipWhile") {
        val subscriptions = CompositeDisposable()
        Observable.just(2, 2, 3, 4)
            .skipWhile { number -> number % 2 == 0 }
            .subscribe { println(it) }
            .addTo(subscriptions)

    }

    exampleOf("skip") {
        val subscriptions = CompositeDisposable()

        subscriptions.add(
            Observable.just("A", "B", "C", "D", "E", "F")
                .skip(3)
                .subscribe { println(it) }
        )
    }

    exampleOf("filter") {
        val subscriptions = CompositeDisposable()

        subscriptions.add(Observable.fromIterable(listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10))
            .filter { number -> number > 5 }
            .subscribe { println(it) }
        )
    }

    exampleOf("elementAt") {
        val subscriptions = CompositeDisposable()
        val strikes = PublishSubject.create<String>()
        subscriptions.add(
            strikes.elementAt(2)
                .subscribeBy(
                    onSuccess = { println("You’re out!") }
                )
        )
        strikes.onNext("1")
        strikes.onNext("2")
        strikes.onNext("3")
    }

    exampleOf("ignoreElements") {
        val subscriptions = CompositeDisposable()

        val strikes = PublishSubject.create<String>()
        strikes.ignoreElements()
            .subscribeBy {
                println("You’re out!")
            }
            .addTo(subscriptions)

        strikes.onNext("1")
        strikes.onNext("2")
        strikes.onNext("3")

        strikes.onComplete()
    }
}

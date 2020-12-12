import com.jakewharton.rxrelay3.PublishRelay
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.subjects.AsyncSubject
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.ReplaySubject

fun main(args: Array<String>) {

    exampleOf("Challenge") {

        val subscriptions = CompositeDisposable()

        val dealtHand = PublishSubject.create<List<Pair<String, Int>>>()

        fun deal(cardCount: Int) {
            val deck = cards
            var cardsRemaining = 52
            val hand = mutableListOf<Pair<String, Int>>()

            (0 until cardCount).forEach { _ ->
                val randomIndex = (0 until cardsRemaining).random()
                hand.add(deck.removeAt(randomIndex))
                cardsRemaining -= 1
            }

            if (points(hand) > 21) {
                dealtHand.onError(HandError.Busted())
            } else {
                dealtHand.onNext(hand)
            }

            subscriptions.dispose()
        }

        subscriptions.add(
            dealtHand.subscribe(
                { hand -> println("${cardString(hand)} for ${points(hand)} points") },
                { println(it) }
            )
        )

        deal(3)
    }

    exampleOf("RxRelay") {
        val subscriptions = CompositeDisposable()

        val publishRelay = PublishRelay.create<Int>()

        subscriptions.add(publishRelay.subscribeBy(
            onNext = { printWithLabel("1)", it) }
        ))

        publishRelay.accept(1)
        publishRelay.accept(2)
        publishRelay.accept(3)
    }

    exampleOf("AsyncSubject") {
        val subscriptions = CompositeDisposable()
        val asyncSubject = AsyncSubject.create<Int>()

        subscriptions.add(asyncSubject.subscribeBy(
            onNext = { printWithLabel("1)", it) },
            onComplete = { printWithLabel("1)", "Complete") }
        ))

        asyncSubject.onNext(0)
        asyncSubject.onNext(1)
        asyncSubject.onNext(2)

        asyncSubject.onComplete()

        subscriptions.dispose()
    }

    exampleOf("ReplaySubject") {

        val subscriptions = CompositeDisposable()

        val replaySubject = ReplaySubject.createWithSize<String>(2)

        replaySubject.onNext("1")
        replaySubject.onNext("2")
        replaySubject.onNext("3")

        subscriptions.add(replaySubject.subscribeBy(
            onNext = { printWithLabel("1)", it) },
            onError = { printWithLabel("1)", it) }
        ))

        subscriptions.add(replaySubject.subscribeBy(
            onNext = { printWithLabel("2)", it) },
            onError = { printWithLabel("2)", it) }
        ))

        replaySubject.onNext("4")

        replaySubject.onError(RuntimeException("Error!"))

        subscriptions.add(replaySubject.subscribeBy(
            onNext = { printWithLabel("3)", it) },
            onError = { printWithLabel("3)", it) }
        ))

    }

    exampleOf("BehaviorSubject") {
        val subscriptions = CompositeDisposable()

        val behaviorSubject = BehaviorSubject.createDefault("Initial value")

        behaviorSubject.onNext("X")

        val subscriptionOne = behaviorSubject.subscribeBy(
            onNext = { printWithLabel("1)", it) },
            onError = { printWithLabel("1)", it) }
        )

        behaviorSubject.onError(RuntimeException("Error!"))

        subscriptions.add(behaviorSubject.subscribeBy(
            onNext = { printWithLabel("2)", it) },
            onError = { printWithLabel("2)", it) }
        ))

    }

    exampleOf("PublishSubject") {
        val publishSubject = PublishSubject
            .create<Int>()

        val subscriptionOne = publishSubject.subscribe { println(it) }

        publishSubject.onNext(1)
        publishSubject.onNext(2)

        val subscriptionTwo = publishSubject.subscribe { int ->
            printWithLabel("2)", int)
        }

        publishSubject.onNext(3)

        subscriptionOne.dispose()

        publishSubject.onNext(4)

        publishSubject.onComplete()

        publishSubject.onNext(5)

        subscriptionTwo.dispose()

        val subscriptionThree = publishSubject.subscribeBy(
            onNext = { printWithLabel("3)", it) },
            onComplete = { printWithLabel("3)", "Complete") }
        )

        publishSubject.onNext(6)
    }

}



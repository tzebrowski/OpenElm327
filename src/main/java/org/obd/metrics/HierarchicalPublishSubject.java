package org.obd.metrics;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.extern.slf4j.Slf4j;
import rx.Observer;
import rx.subjects.PublishSubject;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class HierarchicalPublishSubject<R extends Reply<?>> implements Observer<R> {

	private static final class Reflections {

		String getParameterizedType(Object o) {
			Class<?> clazz = o.getClass();

			while (clazz != null) {
				final Type genericSuperclass = clazz.getGenericSuperclass();
				if (genericSuperclass instanceof ParameterizedType) {
					return getClassName((ParameterizedType) genericSuperclass);
				}
				clazz = clazz.getSuperclass();
			}

			return null;
		}

		private String getClassName(ParameterizedType superClass) {
			final String typeName = (superClass.getActualTypeArguments()[0]).getTypeName();
			final int indexOf = typeName.indexOf("<");
			return indexOf > 0 ? typeName.substring(0, indexOf) : typeName;
		}
	}

	private final Map<String, PublishSubject<R>> publishers = new HashMap<>();
	private final Reflections reflections = new Reflections();

	@Builder
	static HierarchicalPublishSubject<Reply<?>> build(@Singular("observer") List<ReplyObserver<Reply<?>>> observers) {
		final HierarchicalPublishSubject<Reply<?>> instance = new HierarchicalPublishSubject<>();
		observers.forEach(instance::subscribe);
		return instance;
	}

	@Override
	public void onCompleted() {
		publishers.values().forEach((publishSubject) -> publishSubject.onCompleted());
	}

	@Override
	public void onError(Throwable o) {
		publishers.values().forEach((publishSubject) -> publishSubject.onError(o));
	}

	@Override
	public void onNext(R reply) {

		PublishSubject<R> publishSubject = publishers.get(reply.getCommand().getClass().getName());
		if (publishSubject != null) {
			publishSubject.onNext(reply);
		}

		Class<?> clazz = reply.getClass();
		while (clazz != null) {
			publishSubject = publishers.get(clazz.getName());
			if (publishSubject != null) {
				publishSubject.onNext(reply);
			}
			clazz = clazz.getSuperclass();
		}
	}

	private void subscribeFor(ReplyObserver<R> replyObserver, String... types) {
		for (final String type : types) {
			log.info("Subscribing observer: {} for: {}", replyObserver.getClass().getSimpleName(), type);
			findPublishSubjectBy(type).subscribe(replyObserver);
		}
	}

	private void subscribe(ReplyObserver<R> replyObserver) {
		if (replyObserver.observables().length == 0) {
			subscribeFor(replyObserver, reflections.getParameterizedType(replyObserver));
		} else {
			subscribeFor(replyObserver, replyObserver.observables());
		}
	}

	private PublishSubject<R> findPublishSubjectBy(final String type) {
		PublishSubject<R> publishSubject = null;
		if (publishers.containsKey(type)) {
			publishSubject = (PublishSubject<R>) publishers.get(type);
		} else {
			publishSubject = PublishSubject.create();
			publishers.put(type, publishSubject);
		}
		return publishSubject;
	}
}

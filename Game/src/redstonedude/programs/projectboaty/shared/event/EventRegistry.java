package redstonedude.programs.projectboaty.shared.event;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;

import redstonedude.programs.projectboaty.shared.src.Logger;

public class EventRegistry {

	private static ConcurrentLinkedQueue<Class<? extends EventListener>> listeners = new ConcurrentLinkedQueue<Class<? extends EventListener>>();

	/**
	 * Fire an event
	 * 
	 * @param e
	 *            The event to fire
	 */
	public static void fireEvent(Event e) {
		for (Class<? extends EventListener> listenerClass : listeners) {
			dispatchEventTo(e, listenerClass);
		}
	}

	/**
	 * Adds an event listener.
	 */
	public static void addListener(Class<? extends EventListener> listenerClass) {
		listeners.add(listenerClass);
	}

	/**
	 * Removes an event listener.
	 */
	public static void removeListener(Class<? extends EventListener> listenerClass) {
		listeners.remove(listenerClass);
	}

	private static void dispatchEventTo(Event event, Class<? extends EventListener> listenerClass) {
		Collection<Method> methods = findMatchingEventHandlerMethods(listenerClass, event.getClass());
		for (Method method : methods) {
			try {
				// method.setAccessible(true); Don't set accessible - if it's private then that's their loss
				if (Modifier.isStatic(method.getModifiers())) {
					method.invoke(null, event);//static, so self-reference is null
				} else {
					//not static
					Logger.log("Error dispatching event; " +listenerClass.getName() + "." + method.getName() + "() is not static");
				}
			} catch (Exception e) {
				e.printStackTrace();
				Logger.log("Error dispatching event");
			}
		}
	}

	private static Collection<Method> findMatchingEventHandlerMethods(Class<? extends EventListener> listenerClass, Class<? extends Event> eventClass) {
		Method[] methods = listenerClass.getDeclaredMethods();
		Collection<Method> result = new ArrayList<Method>();
		for (Method method : methods) {
			if (canHandleEvent(method, eventClass)) {
				result.add(method);
			}
		}
		return result;
	}

	private static boolean canHandleEvent(Method method, Class<? extends Event> eventClass) {
		Parameter[] parameters = method.getParameters();
		if (parameters.length == 1) {
			if (parameters[0].getType().equals(eventClass)) {
				// If this is an example of event
				return true;
			}

		}
		return false;
	}

}

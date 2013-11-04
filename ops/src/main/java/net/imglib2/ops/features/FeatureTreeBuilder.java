package net.imglib2.ops.features;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.imglib2.Pair;
import net.imglib2.util.ValuePair;

public class FeatureTreeBuilder implements TreeSourceListener
{

	private final List< DescriptorSet > sets;

	private final List< Module< ? > > modules;

	private final Map< Module< ? >, List< Pair< Module< ? >, Field >> > dependencies;

	private final Map< Module< ? >, List< Pair< Module< ? >, Field >>> sourceListenerMap;

	private final FeatureRepository repository;

	private final List< Pair< Module< ? >, Field > > sourceListenerQueue;

	private final List< TreeSource< ? > > sourcesQueue;

	private final List< Descriptor > outputDescriptors;

	public FeatureTreeBuilder()
	{
		this.sets = new ArrayList< DescriptorSet >();
		this.repository = FeatureRepository.getInstance();
		this.sourcesQueue = new ArrayList< TreeSource< ? > >();

		// this can be erased
		this.outputDescriptors = new ArrayList< Descriptor >();
		this.modules = new ArrayList< Module< ? > >();
		this.sourceListenerMap = new HashMap< Module< ? >, List< Pair< Module< ? >, Field >> >();
		this.sourceListenerQueue = new ArrayList< Pair< Module< ? >, Field > >();
		this.dependencies = new HashMap< Module< ? >, List< Pair< Module< ? >, Field >> >();
	}

	private void reset()
	{
		modules.clear();
		sourceListenerQueue.clear();
		sourceListenerMap.clear();
		dependencies.clear();
		outputDescriptors.clear();
	}

	public void registerSource( TreeSource< ? > s )
	{
		sourcesQueue.add( s );
		if ( !s.isRegistered( this ) )
			s.registerListener( this );
	}

	public void registerFeatureSet( DescriptorSet set )
	{
		this.sets.add( set );
	}

	public void build()
	{

		reset();

		// 0. register our sources
		for ( TreeSource< ? > s : sourcesQueue )
		{
			register( s );
		}

		// 1. step: find all features and add them.
		for ( DescriptorSet fs : sets )
		{
			for ( Class< ? extends Descriptor > descriptor : fs.descriptors() )
			{
				registerOutputDescriptor( ( Descriptor ) register( instantiateModule( descriptor ) ) );
			}
		}

		// 2. step parse features for dependencies and set-up graph. Since now
		// we have nothing but a list of features
		List< Module< ? >> helper = new ArrayList< Module< ? >>( modules );
		for ( Module< ? > f : helper )
		{
			parse( f );
		}

		// 3. we have a dependency graph now. let's check for our
		// sourceListeners
		for ( Pair< Module< ? >, Field > pair : sourceListenerQueue )
		{
			Field field = pair.getB();

			Module< ? > source = null;
			for ( Module< ? > f : modules )
			{
				if ( f.isCompatibleOutput( field.getType() ) )
				{
					source = f;
					break;
				}
			}

			// lets check if we found something
			if ( source != null )
			{
				// we found a compatible module. we need to register to
				// listen for updates. here we make use of a different
				// mechanism: we register the parent object with it's
				// field and a reference to the FeatureModule.
				// on each update, the parents get the objects injected
				registerSource( pair, source );
			}
			else
			{
				// TODO:
				// what we could do here: we search our repository for a
				// shortest path to one of our registered FeatureModules etc
				// For now we simply assume any source is present
				throw new IllegalArgumentException( "to be done" );
			}
		}

		// 4. we need to build our dependency tree (inject the depended
		// FeatureModules)
		for ( Entry< Module< ? >, List< Pair< Module< ? >, Field >>> e : dependencies.entrySet() )
		{
			for ( Pair< Module< ? >, Field > dependend : e.getValue() )
			{
				inject( dependend.getA(), dependend.getB(), e.getKey() );
			}
		}

		// we are done. now we can return an iterator over our funny thingy here
	}

	/**
	 * Retrieve the iterator over numeric features
	 * 
	 * @return
	 */
	public Iterator< Descriptor > iterator()
	{
		return outputDescriptors.iterator();
	}

	private void registerOutputDescriptor( Descriptor descriptor )
	{
		outputDescriptors.add( descriptor );
	}

	// TODO: this is dirty, hacky, ugly, ... but really works nice for testing
	// ;-)
	private void inject( Module< ? > mod, Field f, Object object )
	{
		AccessibleObject.setAccessible( new AccessibleObject[] { f }, true );
		try
		{
			f.set( mod, object );
		}
		catch ( IllegalArgumentException e )
		{
			e.printStackTrace();
		}
		catch ( IllegalAccessException e )
		{
			e.printStackTrace();
		}
		AccessibleObject.setAccessible( new AccessibleObject[] { f }, true );
	}

	// recursively mark all dependend features as dirty
	private void markDirty( Module< ? > in )
	{
		for ( Pair< Module< ? >, Field > mod : dependencies.get( in ) )
		{
			if ( !mod.getA().isDirty() )
			{
				mod.getA().markDirty();
				markDirty( mod.getA() );
			}
		}
	}

	private void parse( Module< ? > parent )
	{
		for ( Field annotatedField : parent.getClass().getDeclaredFields() )
		{
			if ( annotatedField.isAnnotationPresent( ModuleInput.class ) )
			{
				// we found a dependency. two types of dependency:
				// auto-instantiable dependencies and
				// not-autoinstantiable-dependencies
				// if we found a FeatureModule

				Class< ? > annotatedType = annotatedField.getType();
				if ( Module.class.isAssignableFrom( annotatedType ) )
				{
					// Instantiate a module from anywhere
					Module< ? > module = instantiateModule( annotatedType );

					// as we now have instantiated a module, we can check
					// whether our features already contains a better one
					Module< ? > registered = register( module );

					// if we found something new, we need to parse it again
					if ( registered == module )
						parse( registered );

					// anyway, we need to register our parent to the module type
					addDependency( registered, new ValuePair< Module< ? >, Field >( parent, annotatedField ) );
				}
				else
				{
					// we didn't find a feature module, so it has to be some
					// native class. We need to find a compatible module! We
					// will do this later
					sourceListenerQueue.add( new ValuePair< Module< ? >, Field >( parent, annotatedField ) );
				}
			}
		}
	}

	private void registerSource( Pair< Module< ? >, Field > pair, Module< ? > source )
	{
		List< Pair< Module< ? >, Field >> listeners = sourceListenerMap.get( source );
		if ( listeners == null )
		{
			listeners = new ArrayList< Pair< Module< ? >, Field > >();
			sourceListenerMap.put( source, listeners );
		}

		listeners.add( pair );
	}

	private void addDependency( Module< ? > module, Pair< Module< ? >, Field > dependend )
	{
		List< Pair< Module< ? >, Field >> list = dependencies.get( module );
		if ( list == null )
		{
			list = new ArrayList< Pair< Module< ? >, Field > >();
			dependencies.put( module, list );
		}
		list.add( dependend );
	}

	private void updateDependencies( Module< ? > oldModule, Module< ? > newModule )
	{
		dependencies.put( newModule, dependencies.get( oldModule ) );
		dependencies.remove( oldModule );
	}

	/**
	 * @param annotatedType
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	private Module< ? > instantiateModule( Class< ? > annotatedType )
	{
		Module< ? > module = null;
		// we found a feature module, so lets try to instantiate it
		if ( Modifier.isInterface( annotatedType.getModifiers() ) || Modifier.isAbstract( annotatedType.getModifiers() ) )
		{
			// find in repository
			module = repository.findFeatureModule( annotatedType, sourcesQueue );
		}
		else
		{
			// emptry constructor has to exist
			try
			{
				// TODO check whether empy contructor exists if not, try repo
				module = ( Module< ? > ) annotatedType.newInstance();
			}
			catch ( InstantiationException e )
			{
				e.printStackTrace();
			}
			catch ( IllegalAccessException e )
			{
				e.printStackTrace();
			}
		}

		return module;
	}

	private Module< ? > register( Module< ? > feature )
	{
		Module< ? > toCheck = feature;
		for ( Module< ? > f : modules )
		{
			if ( f.isEquivalentModule( feature ) )
			{
				toCheck = tryReplace( f, feature );
			}
		}

		if ( !dependencies.containsKey( toCheck ) )
		{
			dependencies.put( feature, new ArrayList< Pair< Module< ? >, Field > >() );
			modules.add( feature );
		}

		return toCheck;
	}

	private Module< ? > tryReplace( Module< ? > oldFeature, Module< ? > newFeature )
	{
		if ( oldFeature.priority() < newFeature.priority() )
		{
			modules.set( modules.indexOf( oldFeature ), newFeature );
			updateDependencies( oldFeature, newFeature );

			if ( outputDescriptors.contains( oldFeature ) )
			{
				outputDescriptors.set( outputDescriptors.indexOf( oldFeature ), ( Descriptor ) newFeature );
			}
			return newFeature;
		}
		else
		{
			return oldFeature;
		}

	}

	@Override
	public void updated( TreeSource< ? > source )
	{
		for ( Pair< Module< ? >, Field > f : sourceListenerMap.get( source ) )
		{
			// First we mark everything as dirty
			Module< ? > mod = f.getA();
			mod.markDirty();
			markDirty( mod );

			// second we inject what ever is needed
			inject( f.getA(), f.getB(), source.get() );

		}
	}
}

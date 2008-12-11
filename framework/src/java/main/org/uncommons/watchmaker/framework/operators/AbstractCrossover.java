// ============================================================================
//   Copyright 2006, 2007, 2008 Daniel W. Dyer
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.
// ============================================================================
package org.uncommons.watchmaker.framework.operators;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import org.uncommons.maths.ConstantGenerator;
import org.uncommons.maths.NumberGenerator;
import org.uncommons.watchmaker.framework.EvolutionaryOperator;
import org.uncommons.watchmaker.framework.Probability;

/**
 * Generic base class for cross-over implementations.  Supports all
 * cross-over processes that operate on a pair of parent candidates.
 * @param <T> The type of evolved candidates that are operated on by
 * this cross-over implementation.
 * @author Daniel Dyer
 */
public abstract class AbstractCrossover<T> implements EvolutionaryOperator<T>
{
    private final NumberGenerator<Integer> crossoverPointsVariable;
    private final Probability crossoverProbability;

    /**
     * Sets up a fixed-point cross-over implementation.  Cross-over is
     * applied to all pairs of parents.  To apply cross-over only to a
     * proportion of parent pairs, use the {@link #AbstractCrossover(int, Probability)}
     * constructor.
     * @param crossoverPoints The constant number of cross-over points
     * to use for all cross-over operations.
     */
    protected AbstractCrossover(int crossoverPoints)
    {
        this(crossoverPoints, Probability.ONE);
    }


    /**
     * Sets up a cross-over implementation that uses a fixed number of cross-over
     * points.  Cross-over is applied to a proportion of selected parent pairs, with
     * the remainder copied unchanged into the output population.  The size of this
     * evolved proportion is controlled by the {@code crossoverProbability} parameter.
     * @param crossoverPoints The constant number of cross-over points
     * to use for all cross-over operations.
     * @param crossoverProbability The probability that, once selected,
     * a pair of parents will be subjected to cross-over rather than
     * being copied, unchanged, into the output population.  Must be in the range
     * {@literal 0 < crossoverProbability <= 1}
     */
    protected AbstractCrossover(int crossoverPoints,
                                Probability crossoverProbability)
    {
        this(new ConstantGenerator<Integer>(crossoverPoints), crossoverProbability);
        if (crossoverPoints <= 0)
        {
            throw new IllegalArgumentException("Number of cross-over points must be positive.");
        }
    }


    /**
     * Sets up a cross-over implementation that uses a variable number of cross-over
     * points.  Cross-over is applied to all pairs of parents.  To apply cross-over
     * only to a proportion of parent pairs, use the
     * {@link #AbstractCrossover(NumberGenerator, Probability)} constructor.
     * @param crossoverPointsVariable A random variable that provides a number
     * of cross-over points for each cross-over operation.
     */
    protected AbstractCrossover(NumberGenerator<Integer> crossoverPointsVariable)
    {
        this(crossoverPointsVariable, Probability.ONE);
    }


    /**
     * Sets up a cross-over implementation that uses a variable number of cross-over
     * points.  Cross-over is applied to a proportion of selected parent pairs, with
     * the remainder copied unchanged into the output population.  The size of this
     * evolved proportion is controlled by the {@code crossoverProbability} parameter.
     * @param crossoverPointsVariable A random variable that provides a number
     * of cross-over points for each cross-over operation.
     * @param crossoverProbability The probability that, once selected,
     * a pair of parents will be subjected to cross-over rather than
     * being copied, unchanged, into the output population.
     */
    protected AbstractCrossover(NumberGenerator<Integer> crossoverPointsVariable,
                                Probability crossoverProbability)
    {
        this.crossoverPointsVariable = crossoverPointsVariable;
        this.crossoverProbability = crossoverProbability;
    }

    
    /**
     * Applies the cross-over operation to the selected candidates.  Pairs of
     * candidates are chosen randomly and subjected to cross-over to produce
     * a pair of offspring candidates.
     * @param selectedCandidates The evolved individuals that have survived to
     * be eligible to reproduce.
     * @param rng A source of randomness used to determine the location of
     * cross-over points.
     * @return The combined set of evolved offspring generated by applying
     * cross-over to the the selected candidates.
     */
    public List<T> apply(List<T> selectedCandidates, Random rng)
    {
        // Shuffle the collection before applying each operation so that the
        // evolution is not influenced by any ordering artifacts from previous
        // operations.
        List<T> selectionClone = new ArrayList<T>(selectedCandidates);
        Collections.shuffle(selectionClone, rng);

        List<T> result = new ArrayList<T>(selectedCandidates.size());
        Iterator<T> iterator = selectionClone.iterator();
        while (iterator.hasNext())
        {
            T parent1 = iterator.next();
            if (!iterator.hasNext())
            {
                // If we have an odd number of selected candidates, we can't pair up
                // the last one so just leave it unmodified.
                result.add(parent1);
            }
            else
            {
                T parent2 = iterator.next();
                // Randomly decide (according to the pre-configured cross-over probability)
                // whether to perform cross-over for these 2 parents.
                int crossoverPoints = crossoverProbability.nextEvent(rng) ? crossoverPointsVariable.nextValue() : 0;
                if (crossoverPoints > 0)
                {
                    result.addAll(mate(parent1, parent2, crossoverPoints, rng));
                }
                else
                {
                    // If there is no cross-over to perform, just add the parents to the
                    // results unaltered.
                    result.add(parent1);
                    result.add(parent2);
                }
            }
        }
        return result;
    }


    /**
     * Implementing classes should return the list elements of the most specific
     * type possible (derived from the actual types of the arguments).  In other
     * words, if {@code parent1} and {@code parent2} are instances of
     * a sub-class of T, then the elements returned returned in the list must
     * also be instances of the same sub-class.  This is to ensure that the
     * cross-over implementation can correctly deal with populations of
     * sub-classes of T.
     * @param parent1 One of two individuals that provides the source material
     * for generating offspring.
     * @param parent2 One of two individuals that provides the source material
     * for generating offspring.
     * @param numberOfCrossoverPoints The number of cross-overs performed on the
     * two parents.
     * @param rng A source of randomness used to determine the location of
     * cross-over points.
     * @return A list containing two evolved offspring.
     */
    protected abstract List<? extends T> mate(T parent1,
                                              T parent2,
                                              int numberOfCrossoverPoints,
                                              Random rng);
}

// ============================================================================
//   Copyright 2006, 2007 Daniel W. Dyer
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
package org.uncommons.watchmaker.framework.selection;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.uncommons.maths.ConstantGenerator;
import org.uncommons.maths.NumberGenerator;
import org.uncommons.watchmaker.framework.EvaluatedCandidate;
import org.uncommons.watchmaker.framework.SelectionStrategy;

/**
 * @author Daniel Dyer
 */
public class TournamentSelection implements SelectionStrategy<Object>
{
    private final NumberGenerator<Double> selectionProbability;

    /**
     * Creates a tournament selection strategy that is controlled by the
     * variable selection probability provided by the specified
     * {@link NumberGenerator}.
     * @param selectionProbability A number generator that produces values in
     * the range {@literal 0.5 < p < 1}.  These values are used as the probability
     * of the fittest candidate being selected in any given tournament.
     */
    public TournamentSelection(NumberGenerator<Double> selectionProbability)
    {
        this.selectionProbability = selectionProbability;
    }

    
    /**
     * Creates a tournament selection strategy with a fixed probability.
     * @param selectionProbability The probability that the fitter of two randomly
     * chosen candidates will be selected.  Since this is a probability it must be
     * between 0.0 and 1.0.  This implementation adds the further restriction that
     * the probability must be greater than 0.5 since any lower value would favour
     * weaker candidates over strong ones, negating the "survival of the fittest"
     * aspect of the evolutionary algorithm.
     */
    public TournamentSelection(double selectionProbability)
    {
        this(new ConstantGenerator<Double>(selectionProbability));
        if (selectionProbability <= 0.5 || selectionProbability >= 1.0)
        {
            throw new IllegalArgumentException("Selection threshold must be greater than 0.5 and less than 1.0.");
        }
    }


    public <S> List<S> select(List<EvaluatedCandidate<S>> population,
                              boolean naturalFitnessScores,
                              int selectionSize,
                              Random rng)
    {
        List<S> selection = new ArrayList<S>(selectionSize);
        for (int i = 0; i < selectionSize; i++)
        {
            // Pick two candidates at random.
            EvaluatedCandidate<S> candidate1 = population.get(rng.nextInt(population.size()));
            EvaluatedCandidate<S> candidate2 = population.get(rng.nextInt(population.size()));

            double probalitity = selectionProbability.nextValue();
            assert probalitity > 0.5 && probalitity < 1 : "Selection probability out-of-range: " + probalitity;

            // Use a random value to decide wether to select the fitter individual or the weaker one.
            double value = rng.nextDouble();
            if (value >= selectionProbability.nextValue() ^ naturalFitnessScores)
            {
                // Select the fitter candidate.
                if (candidate2.getFitness() > candidate1.getFitness())
                {
                    selection.add(candidate2.getCandidate());
                }
                else
                {
                    selection.add(candidate1.getCandidate());
                }
            }
            else
            {
                // Select the less fit candidate.
                if (candidate2.getFitness() > candidate1.getFitness())
                {
                    selection.add(candidate1.getCandidate());
                }
                else
                {
                    selection.add(candidate2.getCandidate());
                }
            }
        }
        return selection;
    }
}

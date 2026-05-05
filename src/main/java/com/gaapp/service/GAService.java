package com.gaapp.service;

import com.gaapp.core.engine.*;
import com.gaapp.core.factory.*;
import com.gaapp.core.fitness.*;
import com.gaapp.core.model.*;
import com.gaapp.core.problem.*;
import com.gaapp.core.selection.SelectionStrategy;
import com.gaapp.core.crossover.CrossoverStrategy;
import com.gaapp.core.mutation.MutationStrategy;
import com.gaapp.core.replacement.ReplacementStrategy;
import com.gaapp.core.util.RandomUtil;
import com.gaapp.core.util.DistanceUtil;

import com.gaapp.dto.*;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GAService {

    private PopulationFactory.GeneType resolveGeneType(GARequest req, ProblemType problemType) {
        return switch (problemType) {
            case TSP -> PopulationFactory.GeneType.INTEGER;
            case KNAPSACK -> PopulationFactory.GeneType.BINARY;
            default -> PopulationFactory.GeneType.valueOf(normalizeText(req.getGeneType()));
        };
    }

    private ProblemConfig buildProblemConfig(GARequest req, ProblemType type) {
        return switch (type) {
            case KNAPSACK -> new KnapsackConfig(
                req.getMinValue(),
                req.getMaxValue(),
                req.getMinWeight(),
                req.getMaxWeight(),
                req.getCapacity()
            );
            case TSP -> new TSPConfig(
                req.getMinCoord(),
                req.getMaxCoord()
            );
            default -> null;
        };
    }

    private FitnessConfig buildFitnessConfig(
        FitnessFactory.FitnessType fitnessType,
        ProblemData problemData,
        GARequest req
    ) {

        return switch (fitnessType) {

            case CUSTOM -> {
                String expr = req.getCustomFormula();
                if (expr == null || expr.trim().isEmpty()) {
                    throw new RuntimeException("Custom fitness formula required");
                }
                yield new CustomFitnessConfig(expr.trim());
            }

            case TSP -> {
                TSPProblem tsp = (TSPProblem) problemData;
                yield new TSPFitnessConfig(tsp.getDistanceMatrix());
            }

            case KNAPSACK -> {
                KnapsackProblem kp = (KnapsackProblem) problemData;
                yield new KnapsackFitnessConfig(
                    kp.getValues(),
                    kp.getWeights(),
                    req.getCapacity()
                );
            }

            default -> null;
        };
    }

    private SelectionStrategy buildSelection(GARequest req) {
        return SelectionFactory.create(
            SelectionFactory.SelectionType.valueOf(normalizeText(req.getSelectionType())),
            req.getTournamentSize()
        ).getStrategy();
    }

    private CrossoverStrategy buildCrossover(FitnessFactory.FitnessType fitnessType, CrossoverFactory.CrossoverType crossoverType) {
        CrossoverFactory.validate(fitnessType, crossoverType);
        return CrossoverFactory.create(crossoverType).getStrategy();
    }

    private MutationStrategy buildMutation(
        FitnessFactory.FitnessType fitnessType, 
        MutationFactory.MutationType mutationType, 
        GARequest req) {

        MutationFactory.validate(fitnessType, mutationType);
        return MutationFactory.create(
            mutationType,
            req.getMinGene(),
            req.getMaxGene()
        ).getStrategy();
    }

    private ReplacementStrategy buildReplacement(GARequest req) {
        return ReplacementFactory.create(
            ReplacementFactory.ReplacementType.valueOf(normalizeText(req.getReplacementType())),
            req.getEliteCount()
        ).getStrategy();
    }

    private int[] toIntArray(List<Integer> list) {
        return list == null ? new int[0] : list.stream().mapToInt(i -> i).toArray();
    }

    private String normalizeText(String s) {
        return s == null ? "NONE" : s.trim().toUpperCase();
    }

    private PopulationBundle buildInitialPopulation(GARequest req, ProblemType type) {

        List<List<String>> table0 = req.getTable("0");

        return switch (type) {

            case GENERIC -> new PopulationBundle(
                buildGenericPopulation(table0,
                        resolveGeneType(req, type),
                        req.getGeneLength()),
                        null
            );

            case TSP -> {
                ProblemData data = buildTSPData(table0);
                Population pop = PopulationFactory.createTSPPopulation(
                    req.getPopulationSize(),
                    req.getGeneLength()
                );
                yield new PopulationBundle(pop, data);
            }

            case KNAPSACK -> {
                ProblemData data = buildKnapsackData(table0, req);
                Population pop = PopulationFactory.createKnapsackPopulation(
                    req.getPopulationSize(),
                    req.getGeneLength()
                );
                yield new PopulationBundle(pop, data);
            }
        };
    }

    private void applySeed(long seed) {
        if (seed != 0) {
            RandomUtil.setSeed(seed);
        } else {
            RandomUtil.setRandomSeed();
        }
    }

    // =========================================================
    // GENERATE 
    // =========================================================
    public PopulationBundle generate(GARequest req) {

        applySeed(req.getSeed() == null ? 0 : req.getSeed());

        ProblemType problemType = ProblemType.valueOf(normalizeText(req.getProblemType()));

        PopulationFactory.GeneType geneType = resolveGeneType(req, problemType);

        ProblemConfig config = buildProblemConfig(req, problemType);

        return PopulationFactory.createProblem(
                problemType,
                req.getPopulationSize(),
                req.getGeneLength(),
                geneType,
                req.getMinGene(),
                req.getMaxGene(),
                config
        );
    }

    // =========================================================
    // RUN
    // =========================================================

    public List<GenerationDTO> run(GARequest req) {

        applySeed(req.getSeed() == null ? 0 : req.getSeed());

        ProblemType problemType = ProblemType.valueOf(normalizeText(req.getProblemType()));

        PopulationFactory.GeneType geneType = resolveGeneType(req, problemType);

        FitnessFactory.FitnessType fitnessType =
                FitnessFactory.FitnessType.valueOf(normalizeText(req.getFitnessType()));

        CrossoverFactory.CrossoverType crossoverType =
            CrossoverFactory.CrossoverType.valueOf(normalizeText(req.getCrossoverType()));

        MutationFactory.MutationType mutationType =
                MutationFactory.MutationType.valueOf(normalizeText(req.getMutationType()));

        List<List<String>> table0 = req.getTable("0");

        PopulationBundle bundle = buildInitialPopulation(req, problemType);
        Population population = bundle.getPopulation();
        ProblemData problemData = bundle.getProblemData();

        FitnessConfig fitnessConfig = buildFitnessConfig(fitnessType, problemData, req);

        FitnessFunction fitness = FitnessFactory.create(
                fitnessType,
                problemData,
                geneType,
                fitnessConfig,
                req.getGeneLength()
        );

        SelectionStrategy selection = buildSelection(req);

        CrossoverStrategy crossover = buildCrossover(fitnessType, crossoverType);

        MutationStrategy mutation = buildMutation(fitnessType, mutationType, req);

        ReplacementStrategy replacement = buildReplacement(req);

        int[] crossoverPoints = toIntArray(req.getCrossoverPoints());

        int[] mutationPoints = toIntArray(req.getMutationPoints());

        GARunner runner = new GARunner(
                new GeneticAlgorithmEngine(),
                req.getMaxGenerations(),
                req.getTargetFitness(),
                req.isApplyTarget(),
                req.getConvergencePatience(),
                req.getConvergenceTolerance(),
                req.isApplyConvergence(),
                req.isApplyDiversity(),
                req.getDiversityThreshold(),
                req.getDiversityPatience(),
                fitness,
                selection,
                crossover,
                mutation,
                replacement,
                req.getCrossoverRate(),
                crossoverPoints,
                req.getMutationRate(),
                mutationPoints
        );

        List<GenerationResult> history = runner.run(population);
        List<GenerationDTO> dtoList = new ArrayList<>();

        for (int i = 0; i < history.size(); i++) {
            GenerationResult gen = history.get(i);
            gen.setEliteCount(req.getEliteCount());

            GenerationDTO dto = toDTO(gen, i, req);
            dtoList.add(dto);
        }
        
        return dtoList;
    }

    private GenerationDTO toDTO(GenerationResult gen, int index, GARequest req) {

        GenerationDTO dto = new GenerationDTO();

        String selectionType = req.getSelectionType();

        dto.setGeneration(index + 1);

        dto.setPopulationBefore(new TableDTO(
            convertPopulationTable(gen.getPopulationBefore(), normalizeText(selectionType))));
        
        dto.setBefore(
            gen.getPopulationBefore().stream()
            .map(this::toIndividualDTO)
            .toList()
        );

        dto.setAfter(
            gen.getNextGeneration().stream()
            .map(this::toIndividualDTO)
            .toList()
        );

        dto.setAfterCrossover(new TableDTO(convertCrossoverTable(gen.getAfterCrossover())));
        dto.setAfterMutation(new TableDTO(convertMutationTable(gen.getAfterMutation())));
        dto.setNewPopulation(new TableDTO(convertPopulation(gen.getNewPopulation())));
        dto.setNextGeneration(new TableDTO(convertPopulationTable(gen.getNextGeneration(), "NONE")));

        dto.setCrossoverFitness(gen.getAfterCrossoverFitness());
        dto.setMutationFitness(gen.getAfterMutationFitness());

        dto.setBestFitness(gen.getBestFitness());
        dto.setAvgFitness(gen.getAvgFitness());
        dto.setBestIndividual(gen.getBestIndividual());
        dto.setStopCriteria(gen.getStopCriteria());

        return dto;
    }

    private IndividualDTO toIndividualDTO(Individual ind) {
        IndividualDTO dto = new IndividualDTO();
        dto.setName(ind.getName());
        dto.setFitness(ind.getFitness());
        dto.setType(ind.getType());
        return dto;
    }


    private Population buildGenericPopulation(List<List<String>> table, 
            PopulationFactory.GeneType geneType, int expectedGeneLength) {

        List<Individual> individuals = new ArrayList<>();

        for (List<String> row : table) {

            String chromosomeStr = row.get(1);

            String[] parts = chromosomeStr.split("-");

            if (parts.length != expectedGeneLength) {
                throw new IllegalArgumentException("Chromosome length mismatch");
            }

            List<Gene> genes = Arrays.stream(parts)
                .map(String::trim)
                .map(part -> parseGene(part, geneType))
                .map(g -> {
                        if (g == null) throw new IllegalArgumentException("Null gene detected");
                        return g;
                })
                .toList();

            individuals.add(new Individual(genes, "X" + (individuals.size() + 1)));
        }

        return new Population(individuals);
    }    


    private ProblemData buildTSPData(List<List<String>> table) {

        int n = table.size();

        List<Point> points = new ArrayList<>();

        for (int i = 0; i < n; i++) {
             double x = Double.parseDouble(table.get(i).get(1));
             double y = Double.parseDouble(table.get(i).get(2));
             points.add(new Point(x, y));
        }

        double[][] matrix = DistanceUtil.buildMatrix(points);

        return new TSPProblem(points, matrix);
    }

    private ProblemData buildKnapsackData(List<List<String>> table, GARequest req) {

        int n = table.size();

        double[] values = new double[n];
        double[] weights = new double[n];

        for (int i = 0; i < n; i++) {
            values[i] = Double.parseDouble(table.get(i).get(1));
            weights[i] = Double.parseDouble(table.get(i).get(2));
        }

        return new KnapsackProblem(values, weights, req.getCapacity());
    }


    public List<List<String>> convertPopulation(Population population) {
        if (population == null) return List.of();
        return convertPopulationTable(population.getIndividuals(), "NONE");
    }

    private List<List<String>> convertPopulationTable(List<Individual> individuals, String selectionType) {

        List<List<String>> table = new ArrayList<>();

        for (Individual ind : individuals) {

            String chromosome = ind.getGenes().stream()
                .map(g -> String.valueOf(g.getValue()))
                .reduce((a, b) -> a + " - " + b)
                .orElse("");

            List<String> row = new ArrayList<>();

            // Base columns
            row.add(ind.getName());                     // X1, O1, etc.
            row.add(chromosome);
            row.add(String.valueOf(ind.getFitness()));

            // Roulette extra columns
            if ("ROULETTE".equalsIgnoreCase(selectionType)) {
                row.add(String.valueOf(ind.getAdjustedFitness()));
                row.add(String.valueOf(ind.getRouletteProbability()));
                row.add(String.valueOf(ind.getExpectedCount()));
            }

            // Rank extra columns
            if ("RANK".equalsIgnoreCase(selectionType)) {
                row.add(String.valueOf(ind.getRank()));
                row.add(String.valueOf(ind.getWeight()));
                row.add(String.valueOf(ind.getRankProbability()));
            }

            table.add(row);
        }

        return table;
    }

    private List<List<String>> convertCrossoverTable(List<Individual> offspring) {

        List<List<String>> table = new ArrayList<>();

        for (Individual child : offspring) {

            String parent1 = formatIndividual(child.getParent1());
            String parent2 = formatIndividual(child.getParent2());

            String childStr = formatIndividual(child);

            table.add(Arrays.asList(
                parent1,
                parent2,
                child.getCrossoverAction(),
                childStr,
                String.valueOf(child.getFitness())
            ));
        }

        return table;
    }

    private List<List<String>> convertMutationTable(List<Individual> individuals) {

        List<List<String>> table = new ArrayList<>();

        for (Individual ind : individuals) {

            String before = formatGenes(ind.getPreMutation());
            String after = formatGenes(ind.getGenes());

            table.add(Arrays.asList(
                ind.getName(),
                before,
                ind.getMutationAction(),
                after,
                String.valueOf(ind.getFitness())
            ));
        }

        return table;
    }

    private String formatGenes(List<Gene> genes) {
        if (genes == null) return "";

        return genes.stream()
            .map(g -> String.valueOf(g.getValue()))
            .reduce((a, b) -> a + " - " + b)
            .orElse("");
    }

    private String formatIndividual(Individual ind) {
        if (ind == null) return "";

        return ind.getName() + " (" + formatGenes(ind.getGenes()) + ")";
    }

    public List<List<String>> convertProblemData(ProblemData data) {

        if (data == null) return List.of();

        // TSP
        if (data instanceof TSPProblem tsp) {

            List<List<String>> table = new ArrayList<>();

            List<Point> points = tsp.getPoints();

            for (int i = 0; i < points.size(); i++) {
                table.add(List.of(
                    String.valueOf(i),
                    String.valueOf(points.get(i).getX()),
                    String.valueOf(points.get(i).getY())
                ));
            }

            return table;
        }

        // KNAPSACK
        if (data instanceof KnapsackProblem kp) {

            List<List<String>> table = new ArrayList<>();

            double[] values = kp.getValues();
            double[] weights = kp.getWeights();

            for (int i = 0; i < values.length; i++) {
                table.add(List.of(
                    String.valueOf(i),
                    String.valueOf(values[i]),
                    String.valueOf(weights[i])
                ));
            }

            return table;
        }

        return List.of();
    }

    private List<String> convertIndividual(Individual ind) {
        if (ind == null) return List.of();

        return List.of(
            formatGenes(ind.getGenes()),
            String.valueOf(ind.getFitness())
        );
    }

    private Gene parseGene(String raw, PopulationFactory.GeneType geneType) {

        if (raw == null || raw.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid gene value: " + raw);
        }

        String value = raw.trim();

        return switch (geneType) {
            case INTEGER -> new IntegerGene(Integer.parseInt(value));
            case BINARY -> new BinaryGene(Integer.parseInt(value));
            case REAL -> new RealGene(Double.parseDouble(value));
        };
    }

}
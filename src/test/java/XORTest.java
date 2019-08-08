import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class XORTest {
    public static void main(String[] args){
        Network net = new Architect().Perceptron(new int[]{2,4,1});

        DataSet[] trainingData = new DataSet[]{
                new DataSet(new double[]{1,1}, new double[]{0}),
                new DataSet(new double[]{1,0}, new double[]{1}),
                new DataSet(new double[]{0,1}, new double[]{1}),
                new DataSet(new double[]{0,0}, new double[]{0}),
        };

        TrainingOptions options = new TrainingOptions();
        options.error = 0.03;
        options.iterations = 1000;

        net.evolve(trainingData,options);


        List<Double> list1 = Arrays.asList(1.0,1.0);
        List<Double> list2 = Arrays.asList(0.0,0.0);
        List<Double> list3 = Arrays.asList(1.0,0.0);
        List<Double> list4 = Arrays.asList(0.0,1.0);

        System.out.println(net.activate(list1, false));
        System.out.println(net.activate(list2, false));
        System.out.println(net.activate(list3, false));
        System.out.println(net.activate(list4, false));
    }
}

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.math.BigDecimal;

/**
 * Fill in the implementation details of the class DecisionTree using this file. Any methods or
 * secondary classes that you want are fine but we will only interact with those methods in the
 * DecisionTree framework.
 * 
 * You must add code for the 1 member and 4 methods specified below.
 * 
 * See DecisionTree for a description of default methods.
 */
public class DecisionTreeImpl extends DecisionTree {
  private DecTreeNode root;
  //ordered list of class labels
  private List<String> labels;
  //ordered list of attributes
  private List<String> attributes;
  //map to ordered discrete values taken by attributes
  private Map<String, List<String>> attributeValues;

  /**
   * Answers static questions about decision trees.
   */
  DecisionTreeImpl() {
    // no code necessary this is void purposefully
  }

  /**
   * Build a decision tree given only a training set.
   *
   * @param train: the training set
   */

  // builds the decision tree using the buildDecisionTree method
  DecisionTreeImpl(DataSet train) {

    this.labels = train.labels;
    this.attributes = train.attributes;
    this.attributeValues = train.attributeValues;
    List<Instance> instances = train.instances;
    this.root = buildDecisionTree(instances, null , null);
  }


  // predict a tested instance label by traversing the decision tree by using the traverseTree method
  @Override
  public String classify(Instance instance) {
    return treeTravere(instance, this.root);
  }

  // calculates the infomation gain for each attributes using the calculateInformationGain method and prints it.
  @Override
  public void rootInfoGain(DataSet train) {
    this.labels = train.labels;
    this.attributes = train.attributes;
    this.attributeValues = train.attributeValues;
    List<Instance> instances = train.instances;
    double entropy = calculateEntropy(instances);
    for (String attribute : attributes) {
      System.out.print("Information gain for " + attribute + ": ");
      double result = calculateInformationGain(instances, attribute, entropy);
      BigDecimal resultToBigDecimal = new BigDecimal(String.valueOf(result)).stripTrailingZeros();
      String formatted = resultToBigDecimal.toPlainString();
      System.out.println(formatted);
    }

  }
  // prints the accuracy of the tested data set , uses classify to check the predicted result
  // and compares it with the actual tested instance label
  @Override
  public void printAccuracy(DataSet test) {
    List<Instance> instances = test.instances;
    int right = 0;
    int wrong = 0;
    String prediction = "";
    String instanceLabel = "";
    for (Instance instance: instances) {
      prediction = classify(instance);
      instanceLabel = instance.label;
      if (prediction.equals(instanceLabel)){
        right += 1;
      } else {
        wrong += 1;
      }
    }
    double accuracyResult = (double) right / (right+wrong);
    System.out.println("The accuracy of the current test is: " + accuracyResult*100 + "%");
  }

  /**
   * Build a decision tree given a training set then prune it using a tuning set.
   * ONLY for extra credits
   *
   * @param train: the training set
   * @param tune:  the tuning set
   */
  DecisionTreeImpl(DataSet train, DataSet tune) {

    this.labels = train.labels;
    this.attributes = train.attributes;
    this.attributeValues = train.attributeValues;
    // TODO: add code here
    // only for extra credits
  }

  @Override
  /**
   * Print the decision tree in the specified format
   */
  public void print() {

    printTreeNode(root, null, 0);
  }

  /**
   * Prints the subtree of the node with each line prefixed by 4 * k spaces.
   */
  public void printTreeNode(DecTreeNode p, DecTreeNode parent, int k) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < k; i++) {
      sb.append("    ");
    }
    String value;
    if (parent == null) {
      value = "ROOT";
    } else {
      int attributeValueIndex = this.getAttributeValueIndex(parent.attribute, p.parentAttributeValue);
      value = attributeValues.get(parent.attribute).get(attributeValueIndex);
    }
    sb.append(value);
    if (p.terminal) {
      sb.append(" (" + p.label + ")");
      System.out.println(sb.toString());
    } else {
      sb.append(" {" + p.attribute + "?}");
      System.out.println(sb.toString());
      for (DecTreeNode child : p.children) {
        printTreeNode(child, p, k + 1);
      }
    }
  }

  /**
   * Helper function to get the index of the label in labels list
   */
  private int getLabelIndex(String label) {
    for (int i = 0; i < this.labels.size(); i++) {
      if (label.equals(this.labels.get(i))) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Helper function to get the index of the attribute in attributes list
   */
  private int getAttributeIndex(String attr) {
    for (int i = 0; i < this.attributes.size(); i++) {
      if (attr.equals(this.attributes.get(i))) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Helper function to get the index of the attributeValue in the list for the attribute key in the attributeValues map
   */
  private int getAttributeValueIndex(String attr, String value) {
    for (int i = 0; i < attributeValues.get(attr).size(); i++) {
      if (value.equals(attributeValues.get(attr).get(i))) {
        return i;
      }
    }
    return -1;
  }

  // new utility methods used to implement the required methods

  // this methods calculate the entropy for the provided list of instaces
  private double calculateEntropy(List<Instance> instances) {
    int numberOfInstances = instances.size();
    Map<String, Integer> labelCount = new HashMap<String, Integer>();

    // go over every instance and count the labels
    for (Instance instance : instances) {
      String label = instance.label;
      labelCount.put(label, labelCount.getOrDefault(label, 0) + 1);
    }

    double entropy = 0.0;

    // calculate the entropy
    for (String label : labelCount.keySet()) {
      int count = labelCount.get(label);
      double proportion = (double) count / numberOfInstances;
      entropy -= proportion * log2(proportion);
    }

    return entropy;

  }

  // this method provided the information gain for a specific attribute and list of instances
  private double calculateInformationGain(List<Instance> instances, String attribute, double entropy) {

    int numberOfInstances = instances.size();
    double remainder = 0.0;
    int attributeNumber = getAttributeIndex(attribute);
    for (String attributeValue : this.attributeValues.get(attribute)) {
      List<Instance> attributeValueSubset = new ArrayList<>();
      for (Instance instance : instances) {
        // create the subset of instance that match each attribute value
        if (instance.attributes.get(attributeNumber).equals(attributeValue)) {
          attributeValueSubset.add(instance);
        }
      }
      // calculate the partial entropy for the current attribute value
      double fraction = (double) attributeValueSubset.size() / numberOfInstances;
      double currentEntropy = calculateEntropy(attributeValueSubset);
      remainder += fraction * currentEntropy;
    }
    double informationGain = entropy - remainder;
    return informationGain;
  }

  // calculate log base 2
  private double log2(double num) {
    return Math.log(num) / Math.log(2);
  }

  // this method builds the decision tree starting from the node
  private DecTreeNode buildDecisionTree(List<Instance> instances, String parentAttributeValue , List<String> attributesSubset ) {

    String maxInfoGainAttribute = "";
    maxInfoGainAttribute = findMaxInfoGainAttribute(instances , attributesSubset);

    // no attribute has a positive infomation gain , make a leaf with a deault label
    if (maxInfoGainAttribute.isEmpty()) {
      return new DecTreeNode(instances.get(0).label, null, parentAttributeValue, true);
    }

    DecTreeNode node = new DecTreeNode(null, maxInfoGainAttribute, parentAttributeValue, false);

    // creates a leaf with the label that all instances have
    if (instancesHaveSameLabel(instances)) {
      node.label = instances.get(0).label;
      node.terminal = true;
      return node;
    }

    int attributeNumber = getAttributeIndex(maxInfoGainAttribute);
    List<String> attributeValues = this.attributeValues.get(maxInfoGainAttribute);
    // creates a child node for each of the attributes' attribute value
    for (String attributeValue : attributeValues) {
      List<Instance> attributeValueSubset = new ArrayList<>();
      for (Instance instance : instances) {
        if (instance.attributes.get(attributeNumber).equals(attributeValue)) {
          attributeValueSubset.add(instance);
        }
      }
      // creates a leaf when there are no instaces with a matching attribute value , uses a default label
      if (attributeValueSubset.isEmpty()) {
        DecTreeNode child = new DecTreeNode(instances.get(0).label, null, attributeValue, true);
        node.addChild(child);
        // adds the current attribute to the attribute subset if it's not null
        // use the attribute subset to avoid repeating attributes in the same branch of the tree
      } else {
        List<String> newAttributesSubset;
        if (attributesSubset == null) {
          newAttributesSubset = new ArrayList<>();
        } else {
          newAttributesSubset = new ArrayList<>(attributesSubset);
          newAttributesSubset.add(maxInfoGainAttribute);
        }
        DecTreeNode child = buildDecisionTree(attributeValueSubset, attributeValue , newAttributesSubset );
        node.addChild(child);
      }

    }

    return node;
  }

  // this method checks if all instances have the same label
  private boolean instancesHaveSameLabel(List<Instance> instances) {
    String label = instances.get(0).label;
    for (Instance instance : instances) {
      if (!instance.label.equals(label)) {
        return false;
      }
    }
    return true;
  }

  // this method finds the attribute that has the highest information gain,
  // only checks attributes that haven't been used by nodes in the current branch
  private String findMaxInfoGainAttribute(List<Instance> instances , List<String> attributesSubset ) {
    double entropy = calculateEntropy(instances);
    double maxInfoGain = 0.0;
    String maxInfoGainAttribute = "";
    // skip any attributes already found in parent nodes in the current branch
    for (String attribute : attributes) {
      if (attributesSubset != null) {
        if (attributesSubset.contains(attribute)) {
          continue;
        }
      }
      double result = calculateInformationGain(instances, attribute, entropy);
      if (result > maxInfoGain) {
        maxInfoGain = result;
        maxInfoGainAttribute = attribute;
      }
    }
    // returns null if there is no attribute that has positive info gain
    return maxInfoGainAttribute;
  }

  // this method traverses the tree according to the given instance attribute values
  private String treeTravere(Instance instance , DecTreeNode node){

    if (node.terminal) {
      return node.label;
    }

    String currAttribute = node.attribute;
    int attributeNumber = getAttributeIndex(currAttribute);
    String instanceAttributeValue= instance.attributes.get(attributeNumber);
    // find the child with the same attribute value as the current instance attribute value for the attribute in the node
    for (DecTreeNode child : node.children){
      if (child.parentAttributeValue.equals(instanceAttributeValue)){
        return treeTravere(instance, child);
      }
    }
    return instance.label; // default val
  }
}

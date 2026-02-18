import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.InputMismatchException;

public class IT103TreeActivity {
    private static final Random random = new Random();
    private static final int MAX_VALUE = 100;
    private static final int MIN_VALUE = 1;
    private static Scanner scanner = new Scanner(System.in);

    static class TreeNode {
        int value;
        List<TreeNode> children;
        int level;

        public TreeNode(int value, int level) {
            this.value = value;
            this.children = new ArrayList<>();
            this.level = level;
        }

        public void addChild(TreeNode child) {
            children.add(child);
        }
    }

    public static void main(String[] args) {
        System.out.println("Welcome to the Tree Data Structure Generator!");

        TreeNode root = new TreeNode(getRandomValue(), 0);

        buildTree(root);

        System.out.println("\nTree Structure:");
        displayTree(root, "");

        ArrayList<Integer> array1D = new ArrayList<>();
        convertToArray1D(root, array1D);
        System.out.println("\n1D Array Representation:");
        System.out.println(array1D);

        ArrayList<ArrayList<Integer>> array2D = new ArrayList<>();
        int maxDepth = findMaxDepth(root);
        for (int i = 0; i <= maxDepth; i++) {
            array2D.add(new ArrayList<>());
        }
        convertToArray2D(root, array2D);
        System.out.println("\n2D Array Representation (by levels):");
        for (int i = 0; i < array2D.size(); i++) {
            System.out.println("Level " + i + ": " + array2D.get(i));
        }


        scanner.close();
    }

    private static void buildTree(TreeNode parent) {
        int numChildren = getValidIntInput("How many children should node with value " + parent.value + " have? ", 0, Integer.MAX_VALUE);

        for (int i = 0; i < numChildren; i++) {
            TreeNode child = new TreeNode(getRandomValue(), parent.level + 1);
            parent.addChild(child);
            System.out.println("Created child node with value: " + child.value);

            int choice = getValidIntInput("Do you want to add children to this node (value: " + child.value + ")? (1:Yes, 0:No) ", 0, 1);
            if (choice == 1) {
                buildTree(child);
            }
        }
    }

    private static int getValidIntInput(String prompt, int min, int max) {
        int input = -1;
        boolean validInput = false;

        while (!validInput) {
            try {
                System.out.print(prompt);
                input = scanner.nextInt();

                if (input >= min && input <= max) {
                    validInput = true;
                } else {
                    System.out.println("Invalid input. Please enter a number between " + min + " and " + max + ".");
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a valid integer.");
                scanner.nextLine();
            }
        }

        return input;
    }

    private static int getRandomValue() {
        return random.nextInt(MAX_VALUE - MIN_VALUE + 1) + MIN_VALUE;
    }

    private static void displayTree(TreeNode node, String indent) {
        System.out.println(indent + "Node(value: " + node.value + ")");
        for (TreeNode child : node.children) {
            displayTree(child, indent + "  ");
        }
    }

    private static void convertToArray1D(TreeNode node, ArrayList<Integer> array) {
        if (node == null) return;

        array.add(node.value);
        for (TreeNode child : node.children) {
            convertToArray1D(child, array);
        }
    }

    private static void convertToArray2D(TreeNode node, ArrayList<ArrayList<Integer>> array) {
        if (node == null) return;

        array.get(node.level).add(node.value);
        for (TreeNode child : node.children) {
            convertToArray2D(child, array);
        }
    }

    private static int findMaxDepth(TreeNode node) {
        if (node == null) return -1;

        int maxDepth = node.level;
        for (TreeNode child : node.children) {
            int childDepth = findMaxDepth(child);
            if (childDepth > maxDepth) {
                maxDepth = childDepth;
            }
        }
        return maxDepth;
    }
}
package forge.gamemodes.rogue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents the path (sequence of nodes) in a Rogue Commander run.
 * For MVP, paths are linear (no branching).
 */
public class RoguePath {

    private List<RoguePathNode> nodes;

    public RoguePath(List<RoguePathNode> nodes) {
        this.nodes = nodes != null ? nodes : new ArrayList<>();
    }

    // Factory method for linear path generation
    public static RoguePath createLinearPath(RoguePathNode... nodes) {
        return new RoguePath(Arrays.asList(nodes));
    }

    public RoguePathNode getNode(int index) {
        if (index >= 0 && index < nodes.size()) {
            return nodes.get(index);
        }
        return null;
    }

    public int getNodeCount() {
        return nodes.size();
    }

    public List<RoguePathNode> getNodes() {
        return nodes;
    }

    public void setNodes(List<RoguePathNode> nodes) {
        this.nodes = nodes;
    }

    // Path queries
    public boolean isComplete() {
        return nodes.stream().allMatch(RoguePathNode::isCompleted);
    }

    public int getCompletedCount() {
        return (int) nodes.stream().filter(RoguePathNode::isCompleted).count();
    }

    /**
     * Get all nodes in a specific row (excludes side nodes with same rowIndex but col=-1).
     * @param rowIndex The row index to filter by
     * @return List of nodes in the specified row
     */
    public List<RoguePathNode> getNodesInRow(int rowIndex) {
        List<RoguePathNode> rowNodes = new ArrayList<>();
        for (RoguePathNode node : nodes) {
            if (node.getRowIndex() == rowIndex && node.getColumnIndex() >= 0) {
                rowNodes.add(node);
            }
        }
        return rowNodes;
    }

    /**
     * Get the maximum row index in the path.
     * @return Highest rowIndex value
     */
    public int getMaxRow() {
        int maxRow = 0;
        for (RoguePathNode node : nodes) {
            if (node.getRowIndex() > maxRow) {
                maxRow = node.getRowIndex();
            }
        }
        return maxRow;
    }

    /**
     * Calculate which nodes in the next row are reachable from the given node.
     * Reachability rules:
     * - Single node in row OR side node (col=-1): connects to ALL nodes in next row
     * - Multi-column plane: connects to nodes at (col-1, col, col+1) in next row
     *
     * @param fromNode The node to check reachability from
     * @return List of reachable nodes in the next row
     */
    public List<RoguePathNode> getReachableNodes(RoguePathNode fromNode) {
        if (fromNode == null) {
            return new ArrayList<>();
        }

        int nextRow = fromNode.getRowIndex() + 1;
        List<RoguePathNode> nextRowNodes = getNodesInRow(nextRow);

        if (nextRowNodes.isEmpty()) {
            return new ArrayList<>();
        }

        // Check if fromNode is single in its row
        List<RoguePathNode> currentRowNodes = getNodesInRow(fromNode.getRowIndex());
        boolean isSingleNode = currentRowNodes.size() == 1;

        // Single nodes connect to ALL in next row
        if (isSingleNode) {
            return nextRowNodes;
        }

        // Multi-column plane: check if it's first/last (side) or middle node
        List<RoguePathNode> reachable = new ArrayList<>();
        int fromCol = fromNode.getColumnIndex();

        // Find min and max column indices in current row
        int minCol = Integer.MAX_VALUE;
        int maxCol = Integer.MIN_VALUE;
        for (RoguePathNode node : currentRowNodes) {
            int col = node.getColumnIndex();
            if (col < minCol) minCol = col;
            if (col > maxCol) maxCol = col;
        }

        boolean isFirstNode = (fromCol == minCol);
        boolean isLastNode = (fromCol == maxCol);

        if (isFirstNode) {
            // First node (leftmost): only reaches first node in next row
            int minNextCol = Integer.MAX_VALUE;
            RoguePathNode firstInNextRow = null;
            for (RoguePathNode node : nextRowNodes) {
                if (node.getColumnIndex() < minNextCol) {
                    minNextCol = node.getColumnIndex();
                    firstInNextRow = node;
                }
            }
            if (firstInNextRow != null) {
                reachable.add(firstInNextRow);
            }
        } else if (isLastNode) {
            // Last node (rightmost): only reaches last node in next row
            int maxNextCol = Integer.MIN_VALUE;
            RoguePathNode lastInNextRow = null;
            for (RoguePathNode node : nextRowNodes) {
                if (node.getColumnIndex() > maxNextCol) {
                    maxNextCol = node.getColumnIndex();
                    lastInNextRow = node;
                }
            }
            if (lastInNextRow != null) {
                reachable.add(lastInNextRow);
            }
        } else {
            // Middle nodes: connect to adjacent columns (col-1, col, col+1)
            for (RoguePathNode node : nextRowNodes) {
                int toCol = node.getColumnIndex();
                if (Math.abs(toCol - fromCol) <= 1) {
                    reachable.add(node);
                }
            }
        }

        return reachable;
    }

    /**
     * Get indices of nodes reachable from the node at the given index.
     * This is the index-based version for UI usage.
     *
     * @param fromNodeIndex The index of the node to check reachability from
     * @return List of indices of reachable nodes
     */
    public List<Integer> getReachableNodeIndices(int fromNodeIndex) {
        if (fromNodeIndex < 0 || fromNodeIndex >= nodes.size()) {
            return new ArrayList<>();
        }

        RoguePathNode fromNode = nodes.get(fromNodeIndex);
        List<RoguePathNode> reachableNodes = getReachableNodes(fromNode);

        List<Integer> indices = new ArrayList<>();
        for (RoguePathNode node : reachableNodes) {
            int index = nodes.indexOf(node);
            if (index >= 0) {
                indices.add(index);
            }
        }

        return indices;
    }

    /**
     * Get indices of visible nodes in the current row based on last completed node in previous row.
     * At the start (first row), all nodes in current row are visible.
     * For subsequent rows, only nodes reachable from the completed node in previous row are visible.
     *
     * @param currentRow The current row index
     * @return List of indices of visible nodes in current row
     */
    public List<Integer> getVisibleNodesInCurrentRow(int currentRow) {
        List<Integer> visibleIndices = new ArrayList<>();

        // Find last completed node in previous row
        int previousRow = currentRow - 1;
        Integer lastCompletedInPrevRow = null;

        if (previousRow >= 0) {
            for (int i = 0; i < nodes.size(); i++) {
                if (nodes.get(i).getRowIndex() == previousRow && nodes.get(i).isCompleted()) {
                    lastCompletedInPrevRow = i;
                    break;
                }
            }
        }

        // Calculate visible nodes
        if (lastCompletedInPrevRow != null) {
            // Return reachable nodes from last completed in previous row
            return getReachableNodeIndices(lastCompletedInPrevRow);
        } else {
            // First row - all nodes in current row are visible
            for (int i = 0; i < nodes.size(); i++) {
                if (nodes.get(i).getRowIndex() == currentRow) {
                    visibleIndices.add(i);
                }
            }
        }

        return visibleIndices;
    }

    @Override
    public String toString() {
        return "Path with " + nodes.size() + " nodes (" + getCompletedCount() + " completed)";
    }
}

package forge.gamemodes.rogue;

import java.util.ArrayList;
import java.util.Comparator;
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
     * Get all nodes in a specific row.
     * @param rowIndex The row index to filter by
     * @return List of nodes in the specified row
     */
    public List<RoguePathNode> getNodesInRow(int rowIndex) {
        List<RoguePathNode> rowNodes = new ArrayList<>();
        for (RoguePathNode node : nodes) {
            if (node.getRowIndex() == rowIndex) {
                rowNodes.add(node);
            }
        }
        // order nodes by columnIndes ascending
        rowNodes.sort(Comparator.comparingInt(RoguePathNode::getColumnIndex));

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
     * - Single node in row: connects to ALL nodes in next row
     * - Multi-node row: first/last nodes connect to first/last in next row, middle nodes connect to adjacent (±1)
     *
     * @param fromNode The node to check reachability from
     * @return List of reachable nodes in the next row
     */
    public List<RoguePathNode> getReachableNodes(RoguePathNode fromNode) {
        if (fromNode == null) {
            return new ArrayList<>();
        }

        // Get nodes in the next row, order by columnIndex
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

        // First node (leftmost):
        if (isFirstNode) {
            // same or smaller size: only reaches first node in next row (col = 0)
            if (nextRowNodes.size() <= currentRowNodes.size()) {
                nextRowNodes.stream()
                    .filter(node -> node.getColumnIndex() == 0)
                    .findFirst()
                    .ifPresent(reachable::add);
            }
            // larger size: reaches first two nodes in next row (col = 0, 1)
            else {
                for (RoguePathNode node : nextRowNodes) {
                    int toCol = node.getColumnIndex();
                    if (toCol == 0 || toCol == 1) {
                        reachable.add(node);
                    }
                }
            }
        // Last  node (rightmost):
        } else if (isLastNode) {
            // same or smaller size: only reaches last node in next row (col = max)
            if (nextRowNodes.size() <= currentRowNodes.size()) {
                // Find max column in next row
                nextRowNodes.stream()
                    .max(java.util.Comparator.comparingInt(RoguePathNode::getColumnIndex))
                    .ifPresent(reachable::add);
            }
            // larger size: reaches last two nodes in next row (col = max, max-1)
            else {
                for (RoguePathNode node : nextRowNodes) {
                    int toCol = node.getColumnIndex();
                    if (toCol == nextRowNodes.size() - 1 || toCol == nextRowNodes.size() - 2) {
                        reachable.add(node);
                    }
                }
            }
        } else {
            // Middle / non-edge nodes

            // if current row and next row have same size,
            // connect only to same column index
            if (nextRowNodes.size() == currentRowNodes.size()) {
                nextRowNodes.stream()
                    .filter(node -> node.getColumnIndex() == fromCol)
                    .findFirst()
                    .ifPresent(reachable::add);

                return reachable;
            }

            // otherwise: connect to adjacent columns

            // if next row has less nodes, shift indices to right by difference of middle position
            int indexShift = 0;
            if (nextRowNodes.size() < currentRowNodes.size()) {
                //get middle index of current row / next row
                double middleIndexCurrent = currentRowNodes.size() / 2.0;
                double middleIndexNext = nextRowNodes.size() / 2.0;

                indexShift = (int) Math.round(middleIndexCurrent - middleIndexNext);
            }

            // if either both col.size of same row and col.size of next row is even or odd,
            // connect only to same column index
            boolean currentRowEven = currentRowNodes.size() % 2 == 0;
            boolean nextRowEven = nextRowNodes.size() % 2 == 0;
            boolean sameParity = currentRowEven == nextRowEven;

            // if current row and next row are both even or both odd, connect only to same column
            // otherwise connect to same column and +1 column
            for (RoguePathNode node : nextRowNodes) {
                int toCol = node.getColumnIndex() + indexShift;
                if (toCol == fromCol || (!sameParity && toCol - fromCol == 1)) {
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

    /**
     * Count how many Planebound rows exist up to and including the given row.
     * Only counts rows that contain at least one NodePlanebound.
     * Used for calculating opponent life (life should only increase with Plane rows, not Sanctum/Bazaar).
     *
     * @param upToRow The row index to count up to (inclusive)
     * @return Number of Planebound rows from 0 to upToRow
     */
    public int countPlaneboundRowsUpTo(int upToRow) {
        int count = 0;
        for (int row = 0; row <= upToRow; row++) {
            List<RoguePathNode> rowNodes = getNodesInRow(row);
            // Check if this row contains any Planebound nodes
            boolean hasPlaneboundNode = rowNodes.stream()
                .anyMatch(NodePlanebound.class::isInstance);
            if (hasPlaneboundNode) {
                count++;
            }
        }
        return count;
    }

    @Override
    public String toString() {
        return "Path with " + nodes.size() + " nodes (" + getCompletedCount() + " completed)";
    }
}

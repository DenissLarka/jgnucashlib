package org.example;

public abstract class CommandLineTool
{
  public void execute(String[] args) throws CouldNotExecuteException
  {
    try
    {
      kernel();
    }
    catch (Exception exc)
    {
      System.err.println("Error in Tool kernel.");
      exc.printStackTrace();
      throw new CouldNotExecuteException();
    }
  }
  
  // -----------------------------------------------------------------
  
  protected abstract void kernel() throws Exception;
}

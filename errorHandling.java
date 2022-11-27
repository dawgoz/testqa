package sample

public class ErrorHandling {

  public User createUser(User user){
      try {
        loadUser(user.getId());
        throw new UserExistsException(user.getId())
      }
      catch (UserNotFoundException exception){
      }
      resolveGroup(user);
      return userRepository.save(user);
  }
  
  public void handleSimulation(Simulation simulation){
    try{
      resultHandler.handle(simulation);
    }
    catch (Throwable t){
      log.error("failed to save result", t);
    }
    finally {
      simulationService.remove(simulation);
    }
  }

  public void validateOwner(String ownerId){
    if(ownerRepository.countTestcasesForOwner(ownerId) == 0){
      throw new ValidationException("owner: " + ownerId + " does not have any testcases. Testcases should be populated...")
    }
  }
  
  public User createUser(User user){
      
      if(loadUser(user.getId()) instanceof User){
        return user;
      };
      
      resolveGroup(user);
      return userRepository.save(user);
  }
    public User editUser(String userId, User user){
      
      if(userId == null){
        throw new UserException("new user can't be edited...");
      };
      return userRepository.update(user);
  }
}
